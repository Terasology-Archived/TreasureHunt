/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.TreasureHunt;

import com.google.api.services.drive.model.App;
import com.google.common.collect.Lists;
import org.terasology.books.logic.BookComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RegisterSystem(RegisterMode.AUTHORITY)
public class hintBook extends BaseComponentSystem{
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    org.terasology.world.WorldProvider WorldProvider;
    @In
    LocalPlayer localPlayer;

    private EntityRef player;
    Optional<Prefab> pHintBook = Assets.getPrefab("TreasureHunt:hintBook");
    BookComponent bcHintBook = pHintBook.get().getComponent(BookComponent.class);

    @ReceiveEvent(components = InventoryComponent.class)
    public void OnPlayerSpawnedEvent (OnPlayerSpawnedEvent event, EntityRef player) {
        //Fetching the hintBook prefab and assigning it to an entity
        //Optional<Prefab> pHintBook = Assets.getPrefab("TreasureHunt:hintBook");
       // BookComponent bcHintBook = pHintBook.get().getComponent(BookComponent.class);
        EntityRef eHintBook = entityManager.create();
        eHintBook.addComponent(bcHintBook);

        //Give book to player
        inventoryManager.giveItem(player, EntityRef.NULL, eHintBook);

        /*Schedule the position to be refreshed every now and then
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(App::refreshPos, 0, 2, TimeUnit.SECONDS);*/



    }

    private void refreshPos (){
        Vector3i playerLocation = getPlayerLocation(player);
        String posX = Integer.toString(playerLocation.getX());
        String posY = Integer.toString(playerLocation.getY());
        String posZ = Integer.toString(playerLocation.getZ());
        bcHintBook.pages = new ArrayList<>(Lists.newArrayList("Player current X pos: " + posX + "                                           " +
                " Player current Y pos: " + posY + "                                           " +
                " Player current Z pos: " + posZ));

    }

    private Vector3i getPlayerLocation(EntityRef player) {
        LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        Vector3f vect = playerLocation.getWorldPosition();
        return new Vector3i(vect.x, vect.y, vect.z);
    }
}