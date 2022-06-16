const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();


// LINE MESSAGE API
const express = require('express');
const line = require('@line/bot-sdk');

// Config
const config = {
  channelAccessToken: 'aPz8G/EYUWSaZFjA3K2aNF1F+TL2iJR8aY10QOZFjU1FxzrSt87koCBIIB5J8+AlEXXUZOnpfZ7IB7ZvSYouX0b724I93iMxXeIRTtSlzFJ+pn87J6wVJ+Br8K6L4x7tfilud4SM8fe5pgfYEqmdwQdB04t89/1O/w1cDnyilFU=',
  channelSecret: '1c341e3a44292a75526f8debb520153a'
};

const client = new line.Client(config);
line.middleware(config);

const app = express();
app.post('/webhook', line.middleware(config), (req, res) => {
  Promise
    .all(req.body.events.map(handleEvent))
    .then((result) => res.json(result));
});

function handleEvent(event) {
  functions.logger.log("LineMessageAPI webhook event", event);

  if (event.type !== 'message' || event.message.type !== 'text') {
    return Promise.resolve(null);
  }

  return client.replyMessage(event.replyToken, {
    type: 'text',
    text: event.message.text
  });
}

// start listen line chat webhook event
app.listen(5576);

// Send push message
//const message = {
//  type: 'text',
//  text: 'Hello World!'
//};
//
//client.pushMessage('<to>', message)
//  .then(() => {
//
//  })
//  .catch((err) => {
//    // error handling
//  });
// exports.sendPushMessage = functions.https.onRequest(())

//exports.LineMessageAPI = functions.https.onRequest((request, response) => {
//  functions.logger.log("LineMessageAPI request", request);
//  functions.logger.log("LineMessageAPI response", response);
//});


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.helloWorld = functions.https.onRequest((request, response) => {
  functions.logger.info("Hello logs!", { structuredData: true });
  response.send("Hello from Firebase!!!");
});

// Take the text parameter passed to this HTTP endpoint and insert it into
// Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into Firestore using the Firebase Admin SDK.
  const writeResult = await admin.firestore().collection('messages').add({ original: original });
  // Send back a message that we've successfully written the message
  res.json({ result: `Message with ID: ${writeResult.id} added.` });
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