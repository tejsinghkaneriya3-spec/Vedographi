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

test("Unauthenticated user cannot read or write user profile or creations", async () => {
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
    unauthDb.collection("users").doc(ALICE_UID).collection("creations").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("c_1")
      .set({
        userId: ALICE_UID,
        title: "Hack",
        content: "Styled",
        kind: "BIO",
        createdAt: past,
        updatedAt: past,
      })
  );
});

test("Owner can create, read, and partially update UserProfile; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Valid create matching Kotlin CloudSyncRepository
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      darkTheme: true,
      favoriteFontIds: ["sans_bold", "bold_script"],
      recentFontIds: ["sans_bold"],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Owner get succeeds; listing /users is blocked
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Cross-user get/update/delete denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      darkTheme: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).delete());

  // Valid partial updates matching Kotlin mutations
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      favoriteFontIds: ["sans_bold", "bold_script", "fraktur_gothic"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      recentFontIds: ["fraktur_gothic", "sans_bold"],
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      darkTheme: false,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject ghost field / role escalation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      darkTheme: true,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Owner can create, list, update, and delete creations; invalid payloads and @DocumentId id key fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Empty collection list succeeds for owner
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("creations").get()
  );

  // Valid create matching CloudSavedCreation (NO 'id' key inside payload)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_1")
      .set({
        userId: ALICE_UID,
        title: "Royal Bio",
        content: "✦ 𝐕𝐄𝐃𝐎𝐆𝐑𝐀𝐏𝐇𝐢 • हर शब्द का अपना अंदाज़! ✦",
        kind: "BIO",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Owner list succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("creations").get()
  );

  // Cross-user list/get/create/delete denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("creations").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_bob")
      .set({
        userId: BOB_UID,
        title: "Injected",
        content: "Bad",
        kind: "BIO",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject payload containing redundant @DocumentId 'id' field
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_with_id")
      .set({
        id: "creation_with_id",
        userId: ALICE_UID,
        title: "Invalid",
        content: "Has id field",
        kind: "DECORATED",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject invalid kind enum
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_bad_kind")
      .set({
        userId: ALICE_UID,
        title: "Invalid",
        content: "Bad kind",
        kind: "UNKNOWN_KIND",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Valid update & delete by owner
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_1")
      .update({
        title: "Updated Royal Bio",
        updatedAt: new Date(Date.now() - 500),
      })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("creations")
      .doc("creation_1")
      .delete()
  );
});
