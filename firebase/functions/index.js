const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();

// Config env file variables
require('dotenv').config()

// LINE MESSAGE API
const express = require('express');
const line = require('@line/bot-sdk');

// LINE Notify SDK
const lineNotifySDK = require('line-notify-sdk');
const lineNotify = new lineNotifySDK();

// Config
const config = {
  channelAccessToken: process.env.LINEBOT_CHANNEL_ACCESS_TOKEN,
  channelSecret: process.env.LINEBOT_CHANNEL_SECRET
};

const lineClient = new line.Client(config);
line.middleware(config);

const app = express();

app.post('/webhook', line.middleware(config), (req, res) => {
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
  functions.logger.log('pushMessage reguest.body', req.body);

  const type = req.body.data.type;

  var timestamp = admin.firestore.FieldValue.serverTimestamp()

  const writeResult = await admin.firestore().collection('events').add({ type: type, timestamp: timestamp });

  functions.logger.log(`write event result = ${writeResult.id}`);

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
    case 'RoomDoorNotClosed':
      console.log('阿嬤房門逾時未關')
      message = {
        type: 'text',
        text: '阿嬤房門逾時未關'
      };
      break;
    default:
      console.log('TODO')
  }

  lineNotify.notify(process.env.LINE_NOTIFY_TOKEN, message.text).then((body) => {
    functions.logger.log('lineNotify success', body);
    res.status(200).json({ data: 'ok' });
  }).catch((e) => {
    functions.logger.log('lineNotify error', e);
    res.status(500).json({ data: '${e}' });
  });

  // send message by group id
  // const groupRef = await admin.firestore().collection('groups');
  /* groupRef.get().then(function (querySnapshot) {
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
  }) */
});

// exports express middleware
exports.app = functions.https.onRequest(app);


// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.helloWorld = functions.https.onRequest((request, response) => {
  response.status(200).send({ data: "Hello from Firebase!!!!!" });
});

exports.notifySticker = functions.https.onRequest((request, response) => {
  functions.logger.log('notifySticker reg.body', request.body);
  // Send a message with sticker
  // https://developers.line.biz/en/docs/messaging-api/sticker-list/#specify-sticker-in-message-object
  lineNotify.notify(process.env.LINE_NOTIFY_TOKEN, '出門嘍', '', '', 446, 1988).then((body) => {
    functions.logger.log('lineNotify success', body);
    response.status(200).json({ data: 'ok' });
  }).catch((e) => {
    functions.logger.log('lineNotify error', e);
    response.status(500).json({ data: e });
  });
});