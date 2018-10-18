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
import com.google.common.collect.Lists;
import org.terasology.books.logic.BookComponent;
import org.terasology.books.rendering.nui.layers.BookScreen;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterSoundComponent;
import org.terasology.logic.characters.events.FootstepEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

@RegisterSystem(RegisterMode.AUTHORITY)
public class HintBook extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    WorldProvider worldProvider;
    @In
    LocalPlayer localPlayer;
    @In
    BlockManager blockManager;

    private EntityRef player;
    private TreasureChest treasureChest;
    Optional<Prefab> pHintBook;
    BookComponent bcHintBook;
    EntityRef eHintBook;

    @ReceiveEvent(components = InventoryComponent.class)
    public void OnPlayerSpawnedEvent (OnPlayerSpawnedEvent event, EntityRef player) {
        this.pHintBook = Assets.getPrefab("TreasureHunt:hintBook");
        this.bcHintBook = pHintBook.get().getComponent(BookComponent.class);

        eHintBook = entityManager.create("TreasureHunt:hintBook");
        eHintBook.addComponent(bcHintBook);

        //Give book to player
        inventoryManager.giveItem(player, EntityRef.NULL, eHintBook);

        Vector3i treasurePos = getRandomTreasurePosition(3, getPlayerLocation(player));
        treasureChest = new TreasureChest(player, treasurePos, blockManager, inventoryManager, entityManager, worldProvider, localPlayer);

        bcHintBook.pages = new ArrayList<>(Lists.newArrayList("Find a treasure"));

    }

    @ReceiveEvent
    public void onFootstep(FootstepEvent event, EntityRef entity, LocationComponent locationComponent, CharacterSoundComponent characterSounds){
        Vector3f vec = locationComponent.getLocalPosition();
        bcHintBook.pages.set(0, String.format("You are %d units away from the treasure!", calculateDistanceFromTheTreasure(vec)));
    }

    private long calculateDistanceFromTheTreasure(Vector3f playerPos) {
        Vector3i vec = vector3fToVector3i(playerPos);
        Vector3i treasurePos = this.treasureChest.getTreasureChestPosition();
        double exp1 = Math.pow(vec.x - treasurePos.x, 2);
        double exp2 = Math.pow(vec.y - treasurePos.y, 2);
        double exp3 = Math.pow(vec.z - treasurePos.z, 2);
        return Math.round(Math.sqrt(exp1 + exp2 + exp3));
    }

    private Vector3i getRandomTreasurePosition(int maxBlocksAway, Vector3i playerPos) {
        Random rnd = new Random();
        int x = playerPos.x + rnd.nextInt(maxBlocksAway + 1);
        int z = playerPos.z + rnd.nextInt(maxBlocksAway + 1);
        /*
        can only spawn lower than player position (I know it sucks...)
        +2 -> we avoid spawning the treasure at the same position as the player.
         */
        int y = playerPos.y - rnd.nextInt(maxBlocksAway + 1) + 1;
        return new Vector3i(x, y, z);
    }

    private Vector3i vector3fToVector3i(Vector3f vec) {
        return new Vector3i(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }

    private Vector3i getPlayerLocation(EntityRef player) {
        LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        Vector3f vect = playerLocation.getWorldPosition();
        return new Vector3i(Math.round(vect.x), Math.round(vect.y), Math.round(vect.z));
    }
}