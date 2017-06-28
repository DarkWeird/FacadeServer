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
package org.terasology.web.resources;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.MessageEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

@RegisterSystem
public class ConsoleResource extends EventEmittingResource<Message> implements DefaultComponentSystem, WritableResource<String> {

    @In
    private Console console;

    ConsoleResource() {
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onMessage(MessageEvent event, EntityRef entityRef) {
        notifyEvent(entityRef, event.getFormattedMessage());
    }

    @Override
    public String getName() {
        return "console";
    }

    @Override
    public Class<String> getDataType() {
        return String.class;
    }

    @Override
    public void write(EntityRef clientEntity, String data) {
        console.execute(data, clientEntity);
    }
}
