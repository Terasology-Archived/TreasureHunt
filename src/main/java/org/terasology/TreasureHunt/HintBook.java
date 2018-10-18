
package org.terasology.TreasureHunt;

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
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

import java.util.ArrayList;
import java.util.Optional;

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

    @ReceiveEvent(components = InventoryComponent.class)
    public void OnPlayerSpawnedEvent (OnPlayerSpawnedEvent event, EntityRef player) {
        this.pHintBook = Assets.getPrefab("TreasureHunt:hintBook");
        this.bcHintBook = pHintBook.get().getComponent(BookComponent.class);
        EntityRef eHintBook = entityManager.create();
        eHintBook.addComponent(bcHintBook);

        inventoryManager.giveItem(player, EntityRef.NULL, eHintBook);

        /*
        We can also randomize the position, but it is a bit difficult since we don't know the surrounding of the player
        ... It can not spawn in the middle of the air for example
         */
        treasureChest = new TreasureChest(player, 3, getPlayerLocation(player).y, 2, blockManager, inventoryManager, entityManager, worldProvider, localPlayer);
        Vector3i treasureLocation = treasureChest.getTreasureChestPosition();

    }


    private Vector3i getPlayerLocation(EntityRef player) {
        LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        Vector3f vect = playerLocation.getWorldPosition();
        return new Vector3i(vect.x, vect.y, vect.z);
    }
}