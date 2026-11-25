const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const ADMIN_UID = "admin_uid_tej";
const ADMIN_EMAIL = "tejsinghkaneriya3@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profile, creations, custom_fonts, or admin config", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      darkTheme: true,
      favoriteFontIds: ["sans_bold"],
      recentFontIds: [],
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("custom_fonts").get()
  );
  await assertFails(
    unauthDb.collection("app_config").doc("monetization").set({
      adsEnabled: false,
      bannerAdUnitId: "ca-app-pub-123/456",
      rewardedAdUnitId: "ca-app-pub-123/789",
      adRewardPoints: 5,
      referralBonusPoints: 2,
      announcementBanner: "",
      updatedBy: ADMIN_EMAIL,
      updatedAt: past,
    })
  );
  // Public monetization config get is allowed, list is denied
  await assertSucceeds(unauthDb.collection("app_config").doc("monetization").get());
  await assertFails(unauthDb.collection("app_config").get());
});

test("Owner can create, read, and update UserProfile wallet & Pro unlocks; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      darkTheme: true,
      favoriteFontIds: ["sans_bold", "bold_script"],
      recentFontIds: ["sans_bold"],
      pointsBalance: 10,
      unlockedProFontIds: ["deco_maharaja"],
      adsWatchedCount: 2,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Valid wallet + Pro unlock update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      pointsBalance: 4,
      unlockedProFontIds: ["deco_maharaja", "hindi_rajwada_crown"],
      adsWatchedCount: 2,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject negative pointsBalance
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      pointsBalance: -5,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject ghost field / role escalation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Owner can CRUD custom_fonts while cross-user access and @DocumentId id key fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_fonts").doc("cf_1").set({
      userId: ALICE_UID,
      name: "My Royal Style",
      charMapEncoded: "a:α,b:в",
      prefix: "꧁ ",
      suffix: " ꧂",
      letterJoiner: "•",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_fonts").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_fonts").get()
  );

  // Reject redundant id key
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_fonts").doc("cf_bad").set({
      id: "cf_bad",
      userId: ALICE_UID,
      name: "Bad",
      charMapEncoded: "a:α",
      prefix: "",
      suffix: "",
      letterJoiner: "",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Referral codes & claims enforce single claim and block self-referral", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    bobDb.collection("referral_codes").doc("BOB12345").set({
      code: "BOB12345",
      ownerUid: BOB_UID,
      ownerName: "Bob",
      createdAt: created,
    })
  );

  // Listing all referral codes is blocked
  await assertFails(aliceDb.collection("referral_codes").get());
  // Direct lookup succeeds
  await assertSucceeds(aliceDb.collection("referral_codes").doc("BOB12345").get());

  // Self-referral fails
  await assertFails(
    bobDb.collection("referral_claims").doc(BOB_UID).set({
      claimerUid: BOB_UID,
      code: "BOB12345",
      referrerUid: BOB_UID,
      bonusPoints: 2,
      createdAt: created,
    })
  );

  // Alice claiming Bob's code succeeds (+2 points)
  await assertSucceeds(
    aliceDb.collection("referral_claims").doc(ALICE_UID).set({
      claimerUid: ALICE_UID,
      code: "BOB12345",
      referrerUid: BOB_UID,
      bonusPoints: 2,
      createdAt: created,
    })
  );
});

test("Admin panel monetization config is writable ONLY by verified tejsinghkaneriya3@gmail.com", async () => {
  const nonAdminDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "random@gmail.com",
    email_verified: true,
  }).firestore();

  const unverifiedAdminSpoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  const verifiedAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  const now = new Date(Date.now() - 1000);
  const validConfig = {
    adsEnabled: true,
    bannerAdUnitId: "ca-app-pub-3940256099942544/6300978111",
    rewardedAdUnitId: "ca-app-pub-3940256099942544/5224354917",
    adRewardPoints: 5,
    referralBonusPoints: 2,
    announcementBanner: "Welcome to VEDOGRAPHi!",
    updatedBy: ADMIN_EMAIL,
    updatedAt: now,
  };

  // Non-admin rejected
  await assertFails(nonAdminDb.collection("app_config").doc("monetization").set(validConfig));
  // Unverified email spoof rejected
  await assertFails(unverifiedAdminSpoofDb.collection("app_config").doc("monetization").set(validConfig));
  // Verified admin succeeds
  await assertSucceeds(verifiedAdminDb.collection("app_config").doc("monetization").set(validConfig));
});
