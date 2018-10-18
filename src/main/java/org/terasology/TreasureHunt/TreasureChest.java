package org.terasology.TreasureHunt;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.logic.location.LocationComponent;
import java.util.ArrayList;


class TreasureChest extends BaseComponentSystem {

    private Vector3i treasureChestPos;
    private BlockManager blockManager;
    private InventoryManager inventoryManager;
    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private LocalPlayer localPlayer;

    TreasureChest(EntityRef player, Vector3i vec, BlockManager bm, InventoryManager im, EntityManager em, WorldProvider wp, LocalPlayer lp) {
        this.blockManager = bm;
        this.inventoryManager = im;
        this.entityManager = em;
        this.worldProvider = wp;
        this.localPlayer = lp;
        this.treasureChestPos = vec;

        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        EntityRef eTreasureChest = configureTreasureChest(blockFactory);
        EntityRef innerChest = configureInnerChest(blockFactory);

        // Place inner chest into golden chest
        inventoryManager.giveItem(eTreasureChest, EntityRef.NULL, innerChest);

        // Add Chest to inventory, because it will spawn it later
        inventoryManager.giveItem(player, EntityRef.NULL, eTreasureChest);

        placeTreasureChest(eTreasureChest, player, treasureChestPos.x, treasureChestPos.y, treasureChestPos.z);
    }

    public void setTreasureChestPosition(int x, int y, int z) {
        this.treasureChestPos.x = x;
        this.treasureChestPos.y = y;
        this.treasureChestPos.z = z;
    }

    public Vector3i getTreasureChestPosition() {
        return this.treasureChestPos;
    }

    /**
     * Configure inner chest. Initialize it and give it objects.
     * @param blockFactory Factory.
     * @return EntityRef
     */
    private EntityRef configureInnerChest(BlockItemFactory blockFactory) {
        EntityRef innerChest = blockFactory.newInstance(blockManager.getBlockFamily("core:Chest"));
        giveItemsToInnerChest(innerChest, blockFactory);
        return innerChest;
    }

    /**
     * Configure Treasure chest. Initialize it and give it objects.
     * @param blockFactory Factory.
     * @return EntityRef
     */
    private EntityRef configureTreasureChest(BlockItemFactory blockFactory) {
        EntityRef eTreasureChest = blockFactory.newInstance(blockManager.getBlockFamily("TreasureHunt:treasureChest"));
        giveItemsToTreasureChest(eTreasureChest, blockFactory);
        return eTreasureChest;
    }

    /**
     * Place a block of bricks (it doesn't matter what kind - just that is attachable) in players view.
     * It also empty the space before that block so that the player can activate the chest (fills it with air).
     * @param playerLocation Location of player.
     * @param x relative x coordinate of the block.
     * @param y  relative y coordinate of the block.
     * @param z relative z coordinate of the block.
     * @return List with index 0 - Location of block, and 1 - Block which was located before insertion.
     */
    private ArrayList<Object> placeBlockRelativeToPlayer(Vector3i pLoc, int x, int y, int z) {
        ArrayList<Object> list = new ArrayList<Object>();
        for(int l=z; l>=0; l--) {
            Block fakeBlock = blockManager.getBlock("engine:air");
            if (l == z)
                fakeBlock = blockManager.getBlock("core:Brick");
            Vector3i vBlockLocation = new Vector3i(pLoc.x + x, pLoc.y + y, pLoc.z + l);
            Block previousBlockOnThisPos = worldProvider.getBlock(vBlockLocation);
            worldProvider.setBlock(vBlockLocation, fakeBlock);
            list.add(vBlockLocation);
            list.add(previousBlockOnThisPos);
        }
        return list;
    }

    /**
     * Places the Treasure chest in the world. Nasty hack for activating items, so change it if u can :D
     * @param chest Chest EntityRef.
     * @param player Player.
     * @param x X coordination of the chest.
     * @param y Y coordination of the chest.
     * @param z Z coordination of the chest.
     */
    private void placeTreasureChest(EntityRef chest, EntityRef player, int x, int y, int z) {
        Vector3i initialPlayerLocation = getPlayerLocation(player);
        /*
        Player must hit the attachable block in order to activate chest. The player position is set a bit further away
        of the chest location, so that we can hit the block.
         */
        teleportPlayerToLocation(player, x, y - 2, z - 2);
        Vector3i newPlayerLocation = getPlayerLocation(player);
        // Here we create attachable block, mentioned in previous comment.
        ArrayList<Object> list = placeBlockRelativeToPlayer(newPlayerLocation, 0, 1, 3);
        localPlayer.activateOwnedEntityAsClient(chest);  // Activate chest
        worldProvider.setBlock((Vector3i)list.get(0), (Block)list.get(1));  // set Changed block to the same as before
        // we skip 2 and 3 because this block is newly created treasure chest...
        worldProvider.setBlock((Vector3i)list.get(4), (Block)list.get(5));  // set Changed block to the same as before
        worldProvider.setBlock((Vector3i)list.get(6), (Block)list.get(7));  // set Changed block to the same as before
        // Move the player back in initial position.
        teleportPlayerToLocation(player, initialPlayerLocation.x, initialPlayerLocation.y, initialPlayerLocation.z);
    }

    /**
     * Teleports player to a position.
     * @param player Player.
     * @param x new X coordinate.
     * @param y new Y coordinate.
     * @param z new Z coordinate.
     */
    private void teleportPlayerToLocation(EntityRef player, int x, int y, int z) {
        LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        playerLocation.setWorldPosition(new Vector3f(x, y, z));
        player.saveComponent(playerLocation);
    }


    /**
     * Returns player's location in the world.
     * @param player Player.
     * @return location.
     */
    private Vector3i getPlayerLocation(EntityRef player) {
        LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        Vector3f vect = playerLocation.getWorldPosition();
        return new Vector3i(vect.x, vect.y, vect.z);
    }


    /**
     * Give items to the chest. It can be changed easily.
     * @param entity chest.
     * @param blockFactory blockFactory.
     */
    private void giveItemsToInnerChest(EntityRef entity, BlockItemFactory blockFactory) {
        entity.addComponent(new InventoryComponent(30));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:lava"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:water"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Iris"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Dandelion"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Tulip"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:YellowFlower"), 10));
    }

    /**
     * Give items to the chest. It can be changed easily.
     * @param entity chest.
     * @param blockFactory blockFactory.
     */
    private void giveItemsToTreasureChest(EntityRef entity, BlockItemFactory blockFactory) {
        entity.addComponent(new InventoryComponent(30));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Tnt"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, entityManager.create("core:fuseShort"));
        inventoryManager.giveItem(entity, EntityRef.NULL, entityManager.create("core:fuseLong"));
        inventoryManager.giveItem(entity, EntityRef.NULL, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Brick"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Ice"), 10));
        inventoryManager.giveItem(entity, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("core:Plank"), 10));
    }

}