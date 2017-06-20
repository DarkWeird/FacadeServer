/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web.webSocket;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.io.ActionResult;
import org.terasology.web.io.JsonSession;

/**
 * Manages one websocket session
 */
public class WebSocketHandler extends WebSocketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private JsonSession jsonSession;

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        logger.info("Connected: " + session.getRemoteAddress());
        jsonSession = new JsonSession();
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        try {
            ClientMessage deserializedMessage = GSON.fromJson(message, ClientMessage.class);
            deserializedMessage.checkValid();
            handleClientMessage(deserializedMessage);
        } catch (JsonSyntaxException ex) {
            trySendResult(ActionResult.JSON_PARSE_ERROR);
        } catch (InvalidClientMessageException ex) {
            trySendResult(new ActionResult(ActionResult.Status.BAD_REQUEST, ex.getMessage()));
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        jsonSession.disconnect();
        jsonSession = null;
        logger.info("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        logger.error("Error", cause);
    }

    private void handleClientMessage(ClientMessage clientMessage) {
        switch(clientMessage.getMessageType()) {
            case AUTHENTICATION_REQUEST:
                trySendResult(jsonSession.initAuthentication()); //send server handshake hello
                break;
            case AUTHENTICATION_DATA:
                trySendResult(jsonSession.finishAuthentication(clientMessage.getData())); //process client handshake hello
                break;
            case RESOURCE:

        }
    }

    private void trySend(String message) {
        try {
            getSession().getRemote().sendString(message);
        } catch (IOException e) {
            logger.warn("Unable to send message!", e);
        }
    }

    private void trySendResult(ActionResult result) {
        trySend(result.toJsonString(GSON));
    }
}
