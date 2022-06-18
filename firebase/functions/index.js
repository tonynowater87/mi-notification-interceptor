const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();

// Config env file variables
require('dotenv').config()

// LINE MESSAGE API
const express = require('express');
const line = require('@line/bot-sdk');

// Config
const config = {
  channelAccessToken: process.env.LINEBOT_CHANNEL_ACCESS_TOKEN,
  channelSecret: process.env.LINEBOT_CHANNEL_SECRET
};

const lineClient = new line.Client(config);
line.middleware(config);

const app = express();
app.post('/webhook', line.middleware(config), (req, res) => {
  functions.logger.log("LineMessageAPI webhook request", req);
  functions.logger.log("LineMessageAPI webhook response", res);
  Promise
    .all(req.body.events.map(handleEvent))
    .then((result) => res.status(200).json(result));
});

async function handleEvent(event) {
  functions.logger.log("LineMessageAPI webhook event", event);

  if (event.type == 'join') {
    const writeResult = await admin.firestore().collection('groups').add({ group: event.source.groupId });
    functions.logger.log(`webhook join event, added data ${writeResult.id}`);
    return Promise.resolve(null);
  }

  if (event.type == 'leave') {
    const groupRef = await admin.firestore().collection('groups').where('group', '==', event.source.groupId);
    groupRef.get().then(function (querySnapshot) {
      querySnapshot.forEach(function (doc) {
        functions.logger.log(`webhook leave event, remove data ${doc.ref}`);
        doc.ref.delete();
      });
    })
    return Promise.resolve(null);
  }

  if (event.type !== 'message' || event.message.type !== 'text') {
    return Promise.resolve(null);
  }

  return Promise.resolve(null);
}

exports.pushMessage = functions.https.onRequest(async (req, res) => {
  functions.logger.log('reg.body', req.body);

  const type = req.body.data.type;

  functions.logger.log(`message query = ${type}, ${timestamp}`);

  var timestamp = admin.firestore.FieldValue.serverTimestamp()

  const writeResult = await admin.firestore().collection('events').add({ type: type, timestamp: timestamp });

  functions.logger.log(`write event result = ${writeResult.id}`);

  const groupRef = await admin.firestore().collection('groups');

  var message;

  switch (type) {
    case 'OpenRoomDoor':
      console.log('阿嬤開啟房門')
      message = {
        type: 'text',
        text: '阿嬤房門開啟'
      };
      break;
    case 'CloseRoomDoor':
      console.log('阿嬤關閉房門')
      message = {
        type: 'text',
        text: '阿嬤房門關閉'
      };
      break;
    case 'UpDownStairs':
      console.log('偵測阿嬤從三樓走下二樓')
      message = {
        type: 'text',
        text: '阿嬤疑似於睡覺時間(00:00～06:30)從三樓走下二樓'
      };
      break;
    default:
      console.log('TODO')
  }

  groupRef.get().then(function (querySnapshot) {
    querySnapshot.forEach(function (doc) {
      var groupId = doc.data().group;
      functions.logger.log(`push message, doc.data = ${groupId}`);
      lineClient.pushMessage(groupId, message)
        .then(() => {
          functions.logger.log(`pushed message to groupId = ${groupId}`);
          res.status(200).json({ result: `Pushed Message Successfully` });
        })
        .catch((err) => {
          functions.logger.log(`pushed message error ${err}, groupId = ${groupId}`);
          res.status(500).json({ result: `Pushed Message Failed` });
        });
    });
  })
});

// exports express middleware
exports.app = functions.https.onRequest(app);


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.helloWorld = functions.https.onRequest((request, response) => {
  response.status(200).send({ data: "Hello from Firebase!!!!!" });
});

// Take the text parameter passed to this HTTP endpoint and insert it into
// Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
  functions.logger.log('reg.body', req.body);
  //const body = JSON.parse(req.body);
  const original = req.body.data.text;
  // Push the new message into Firestore using the Firebase Admin SDK.
  const writeResult = await admin.firestore().collection('messages').add({ original: original });
  // Send back a message that we've successfully written the message
  res.status(200).json({ result: `Message with ID: ${writeResult.id} added.` });
});

// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
  .onCreate((snap, context) => {
    // Grab the current value of what was written to Firestore.
    const original = snap.data().original;

    // Access the parameter `{documentId}` with `context.params`
    functions.logger.log('Uppercasing', context.params.documentId, original);

    const uppercase = original.toUpperCase();

    // You must return a Promise when performing asynchronous tasks inside a Functions such as
    // writing to Firestore.
    // Setting an 'uppercase' field in Firestore document returns a Promise.
    return snap.ref.set({ uppercase }, { merge: true });
  });