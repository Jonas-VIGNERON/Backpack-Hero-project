package game.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import affichage.Direction;
import game.backpack.Backpack;
import game.context.Context;
import game.effect.Effect;
import game.effect.EffectType;
import game.entity.Target;
import game.entity.monsters.ListOfMonster;
import game.entity.monsters.moves.TypeOfMoves;
import game.pos.Pos;

/**
 * Class used to create items.
 */
public class ItemsCreation {

	/**
	 * Sets the hero's last move and update the living shadow move if there is one
	 * in the monster's team
	 * 
	 * @param t       the move's type
	 * @param context contains informations about the current fight.
	 */
	private static void setLastMoveAndUpdateMonsters(TypeOfMoves t, Context context) {
		context.hero().setLastMove(t);
		ListOfMonster.updateIfLivingShadow(context.monsters());
	}

	/**
	 * Adds an item located at the given coordinates to the list of buffed items if
	 * it satisfies all required conditions.
	 * <p>
	 * An item is added if:
	 * <ul>
	 * <li>There is an item at the given position</li>
	 * <li>The item is a real item (not VOID or BLOCKED)</li>
	 * <li>The item is not the source item itself</li>
	 * <li>The item has not already been added to the list</li>
	 * </ul>
	 *
	 * @param bp     the backpack containing the items grid
	 * @param list   the list of items to be buffed
	 * @param source the item that provides the bonus (must not buff itself)
	 * @param x      the x-coordinate to check
	 * @param y      the y-coordinate to check
	 */
	private static void addIfValid(Backpack bp, List<Item> list, Item source, int x, int y) {
		var item = bp.getItemFromPos(new Pos(x, y));
		if (item != null && item.isAnItem() && item != source && !list.contains(item)) {
			list.add(item);
		}
	}

	/**
	 * Applies a boost for items adjacent to the item which provide the boost.
	 * 
	 * @param context
	 * @param boost
	 * @return
	 */
	private static void bonusForAdjacentItems(Context context, Consumer<Item> boost) {
		var bp = context.backpack();
		var source = context.itemSource();
		var shape = source.getShape();
		var pos = source.getPos();

		var buffedItems = new ArrayList<Item>();

		for (int row = 0; row < shape.lines(); row++) {
			for (int col = 0; col < shape.columns(); col++) {
				if (!shape.shape()[row][col])
					continue;
				addIfValid(bp, buffedItems, source, pos.x() + col, pos.y() + row - 1);
				addIfValid(bp, buffedItems, source, pos.x() + col, pos.y() + row + 1);
				addIfValid(bp, buffedItems, source, pos.x() + col - 1, pos.y() + row);
				addIfValid(bp, buffedItems, source, pos.x() + col + 1, pos.y() + row);
			}
		}
		buffedItems.forEach(boost);
	}

	/**
	 * Modify the damage deal by an item by applying effects and the boosts they
	 * could get.
	 * 
	 * @param damage  the initial damage
	 * @param context the context
	 * @return the new amount of damage after applying effects and their boost
	 */
	private static int damageDealsAfterBoost(int damage, Context context) {
		return Target.damageDealsWithEffects(context.hero(), damage + context.itemSource().getBonusDamage());
	}
	
	private static int shieldAfterBoost(int amount, Context context) {
		return Target.shieldAfterEffects(context.hero(), amount + context.itemSource().getBonusShield());
	}

	/***************************** Void and Blocked ********************/

	/**
	 * Create a Void item Void is an empty item, and it is possible to replace it by
	 * another item but Void and Blocked.
	 * 
	 * @return Item
	 */
	public static Item createVoid() {
		return new Item("Void", ItemType.VOID, Rarity.COMMON, Shape.getA1By1Shape(), false, false, 0, null, null);
	}

	/**
	 * Create a Blocked item Blocked is an empty item, and it is not possible to
	 * replace it by another item. In the backpack it represents a locked slot. And
	 * can become a Void item when the player decides to unlock this slot.
	 *
	 * @return Item
	 */
	public static Item createBlocked() {
		return new Item("Blocked", ItemType.BLOCKED, Rarity.COMMON, Shape.getA1By1Shape(), false, false, 0, null, null);
	}

	/**************************** Weapons ******************************/

	/**
	 * Creates a Wooden Sword
	 * 
	 * @return Item
	 */
	public static Item createWoodenSword() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceEnergy(1)) {
				var damage = damageDealsAfterBoost(7, context);
				context.getTargetedMonster().takeDamage(damage);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true }, { true }, { true } };
		return new Item("WoodenSword", ItemType.WEAPON, Rarity.COMMON, new Shape(s, 1, 3, Direction.UP), true, true,
				-1, consumerForOnUse, null);
	}

	/**
	 * Creates a Dart
	 * 
	 * @return Item
	 */
	public static Item createDart() {
		;
		Consumer<Context> consumerForOnUse = context -> {
			var item = context.itemSource();
			context.monsters().forEach(m -> {
				var damage = damageDealsAfterBoost(10, context);
				m.takeDamage(damage);
			});
			item.setNumberOfUses(item.getNumberOfUses() - 1);
			setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
		};
		return new Item("Dart", ItemType.WEAPON, Rarity.COMMON, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/**
	 * Creates a Flame Axe
	 * 
	 * @return Item
	 */
	public static Item createFlameAxe() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			var target = context.getTargetedMonster();
			if (hero.reduceEnergy(1)) {
				var damage = damageDealsAfterBoost(4, context);
				target.takeDamage(damage);
				Effect.addEffect(target, EffectType.BURN, 4);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true, true }, { true, false } };
		return new Item("FlameAxe", ItemType.WEAPON, Rarity.LEGENDARY, new Shape(s, 2, 2, Direction.UP), true, true,
				-1, consumerForOnUse, null);
	}

	/**
	 * Creates a Jade Axe
	 * 
	 * @return Item
	 */
	public static Item createJadeAxe() {
		Consumer<Context> consumerForOnUse = context -> {
			var bp = context.backpack();
			if (bp.reduceGold(2)) {
				var damage = damageDealsAfterBoost(14, context);
				var target = context.getTargetedMonster();
				target.takeDamage(damage);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
				bonusForAdjacentItems(context, i -> {
					if (i.getInfos().type() == ItemType.WEAPON) {
						i.addBonusDamage(1);
					}
				});
			}
		};
		boolean[][] s = { { true, true }, { true, false }, { true, false } };
		return new Item("JadeAxe", ItemType.WEAPON, Rarity.LEGENDARY, new Shape(s, 2, 3, Direction.UP), true, true,
				-1, consumerForOnUse, null);
	}

	/**
	 * Creates a Spiky Club
	 * 
	 * @return Item
	 */
	public static Item createSpikyClub() {
		Consumer<Context> consumerForOnUse = context -> {
			if (context.hero().reduceEnergy(1)) {
				var damage = damageDealsAfterBoost(7, context);
				var target = context.getTargetedMonster();
				target.takeDamage(damage);
				Effect.addEffect(target, EffectType.SLOW, 1);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true }, { true } };
		return new Item("SpikyClub", ItemType.WEAPON, Rarity.UNCOMMON, new Shape(s, 1, 2, Direction.UP), true, true,
				-1, consumerForOnUse, null);
	}

	/**
	 * Creates a Vampiric Axe
	 * 
	 * @return Item
	 */
	public static Item createVampiricAxe() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceEnergy(1)) {
				var damage = damageDealsAfterBoost(10, context);
				context.getTargetedMonster().takeDamage(damage);
				Effect.addEffect(hero, EffectType.POISON, 3);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true }, { true }, { true } };
		return new Item("VampiricAxe", ItemType.WEAPON, Rarity.RARE, new Shape(s, 1, 3, Direction.UP), true, true,
				-1, consumerForOnUse, null);
	}

	/**
	 * Creates an Earthstone Blade
	 * 
	 * @return Item
	 */
	public static Item createEarthstoneBlade() {
		Consumer<Context> consumerForOnUse = context -> {
			if (context.hero().reduceEnergy(1)) {
				var item = context.itemSource();
				var pos = item.getPos();
				var moreDamage = context.backpack().countRowInDirection(pos, item.getShape(), Direction.DOWN);
				var damage = 5 + moreDamage * 3;
				damage = damageDealsAfterBoost(damage, context);
				context.getTargetedMonster().takeDamage(damage);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true }, { true }, { true }, { true } };
		return new Item("EarthstoneBlade", ItemType.WEAPON, Rarity.LEGENDARY, new Shape(s, 1, 4, Direction.UP),
				true, true, -1, consumerForOnUse, null);
	}

	/**
	 * Creates a Frozen Hammer
	 * 
	 * @return Item
	 */
	public static Item createFrozenHammer() {
		Consumer<Context> consumerForOnUse = context -> {
			if (context.hero().reduceEnergy(1)) {
				var damage = damageDealsAfterBoost(2, context);
				var target = context.getTargetedMonster();
				target.takeDamage(damage);
				Effect.addEffect(target, EffectType.FREEZE, 3);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true, true }, { true, true } };
		return new Item("FrozenHammer", ItemType.WEAPON, Rarity.LEGENDARY, new Shape(s, 2, 2, Direction.UP), true,
				true, -1, consumerForOnUse, null);
	}

	/**
	 * Creates a Mace
	 * 
	 * @return Item
	 */
	public static Item createMace() {
		Consumer<Context> consumerForOnUse = context -> {
			if (context.hero().reduceEnergy(2)) {
				var damage = damageDealsAfterBoost(16, context);
				var target = context.getTargetedMonster();
				target.takeDamage(damage);
				Effect.addEffect(target, EffectType.SLOW, 2);
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { true }, { true } };
		return new Item("Mace", ItemType.WEAPON, Rarity.RARE, new Shape(s, 1, 2, Direction.UP), true, true, -1,
				consumerForOnUse, null);
	}

	/**
	 * Creates a Damaged Knife
	 * 
	 * @return Item
	 */
	public static Item createDamagedKnife() {
		Consumer<Context> consumerForOnUse = context -> {
			var damage = damageDealsAfterBoost(8, context);
			context.getTargetedMonster().takeDamage(damage);
			var item = context.itemSource();
			item.setNumberOfUses(item.getNumberOfUses() - 1);
			setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
		};
		boolean[][] s = { { true }, { true } };
		return new Item("DamagedKnife", ItemType.WEAPON, Rarity.COMMON, new Shape(s, 1, 2, Direction.UP), true,
				false, 3, consumerForOnUse, null);
	}

	/**
	 * Creates a Throwing Star
	 * 
	 * @return Item
	 */
	public static Item createThrowingStar() {
		Consumer<Context> consumerForOnUse = context -> {
			var damage = damageDealsAfterBoost(6, context);
			context.getTargetedMonster().takeDamage(damage);
			var item = context.itemSource();
			item.setNumberOfUses(item.getNumberOfUses() - 1);
			setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
		};
		return new Item("ThrowingStar", ItemType.WEAPON, Rarity.COMMON, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/**
	 * Creates a Poison Star
	 * 
	 * @return Item
	 */
	public static Item createPoisonStar() {
		Consumer<Context> consumerForOnUse = context -> {
			var damage = damageDealsAfterBoost(3, context);
			var target = context.getTargetedMonster();
			target.takeDamage(damage);
			Effect.addEffect(target, EffectType.POISON, 3);
			var item = context.itemSource();
			item.setNumberOfUses(item.getNumberOfUses() - 1);
			setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
		};
		return new Item("PoisonStar", ItemType.WEAPON, Rarity.COMMON, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/********************************* Shields *****************************/
	/**
	 * Creates a Rough Buckler
	 * 
	 * @return Item
	 */
	public static Item createRoughBuckler() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceEnergy(1)) {
				var shield = shieldAfterBoost(7, context);
				hero.addShield(shield);
				setLastMoveAndUpdateMonsters(TypeOfMoves.SHIELD, context);
			}
		};
		boolean[][] s = { { true, true }, { true, true } };
		return new Item("RoughBuckler", ItemType.SHIELD, Rarity.COMMON, new Shape(s, 2, 2, Direction.UP), true,
				true, -1, consumerForOnUse, null);
	}

	/**
	 * Creates a Sweaty Towel
	 * 
	 * @return Item
	 */
	public static Item createSweatyTowel() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceEnergy(1)) {
				var shield = shieldAfterBoost(12, context);
				hero.addShield(shield);
				Effect.addEffect(hero, EffectType.BURN, 3);
				setLastMoveAndUpdateMonsters(TypeOfMoves.SHIELD, context);
			}
		};
		boolean[][] s = { { true }, { true } };
		return new Item("SweatyTowel", ItemType.SHIELD, Rarity.UNCOMMON, new Shape(s, 1, 2, Direction.UP), true,
				true, -1, consumerForOnUse, null);
	}

	/**
	 * Creates the consumer for before turn for Tower Shield
	 * 
	 * @return
	 */
	private static Consumer<Context> towerShieldConsumerBeforeTurn() {
		Consumer<Context> c = context -> {
			var setItemsGetBoost = new HashSet<Item>();
			var item = context.itemSource(); // It represents the TowerShield
			var pos = item.getPos();
			for (var line = pos.y(); line < pos.y() + item.getShape().lines() - 1; line++) {
				for (var column = pos.x() - 1; column >= 0; column--) {
					var i = context.backpack().getItemFromPos(new Pos(column, line));
					if (i.getInfos().type() == ItemType.ARMOR) {
						setItemsGetBoost.add(i);
					}
				}
			}
			setItemsGetBoost.forEach(i -> i.addBonusShield(2));
		};
		return c;
	}

	/**
	 * Creates a Tower Shield
	 * 
	 * @return Item
	 */
	public static Item createTowerShield() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceEnergy(1)) {
				var shield = shieldAfterBoost(5, context);
				hero.addShield(shield);
				setLastMoveAndUpdateMonsters(TypeOfMoves.SHIELD, context);
			}
		};
		Consumer<Context> consumerForBeforeTurn = towerShieldConsumerBeforeTurn();
		boolean[][] s = { { true }, { true } };
		return new Item("TowerShield", ItemType.SHIELD, Rarity.RARE, new Shape(s, 1, 2, Direction.UP), true, true,
				-1, consumerForOnUse, consumerForBeforeTurn);
	}

	/**
	 * Creates a Ring of Rage
	 * 
	 * @return Item
	 */
	public static Item createRingOfRage() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			IO.println("Je passe la : " + hero.getMana());
			if (hero.reduceMana(1)) {
				Effect.addEffect(hero, EffectType.RAGE, 1);
				setLastMoveAndUpdateMonsters(TypeOfMoves.SHIELD, context);
			}
		};
		return new Item("RingOfRage", ItemType.SHIELD, Rarity.RARE, Shape.getA1By1Shape(), true, true, -1,
				consumerForOnUse, null);
	}

	/******************************** Gems ***************************/

	/**
	 * Creates a Sapphire
	 * 
	 * @return Item
	 */
	public static Item createSapphire() {
		Consumer<Context> consumerForBeforeTurn = context -> bonusForAdjacentItems(context, i -> {
			if (i.getInfos().type() == ItemType.WEAPON) {
				i.addBonusDamage(1);
			}
		});
		return new Item("Sapphire", ItemType.GEM, Rarity.COMMON, Shape.getA1By1Shape(), false, false, -1, null,
				consumerForBeforeTurn);
	}

	/******************************** Gold ****************************/

	/**
	 * Creates a Gold
	 * 
	 * @return Item
	 */
	public static Item createGold(int golds) {
		if (golds < 0) {
			throw new IllegalArgumentException("golds must not be negative");
		}
		return new Item("Gold", ItemType.GOLD, Rarity.COMMON, Shape.getA1By1Shape(), false, false, golds, null,
				null);
	}

	/********************************* Armor **************************/

	/**
	 * Creates a Leather Cap
	 * 
	 * @return Item
	 */
	public static Item createLeatherCap() {
		Consumer<Context> consumerForBeforeTurn = context -> {
			var item = context.itemSource();
			var pos = item.getPos();
			var shield = context.backpack().countRowInDirection(pos, item.getShape(), Direction.DOWN);
			shield = shieldAfterBoost(shield, context);
			context.hero().addShield(shield);
		};
		return new Item("LeatherCap", ItemType.ARMOR, Rarity.COMMON, Shape.getA1By1Shape(), false, false, -1, null,
				consumerForBeforeTurn);
	}

	/**
	 * Creates a Plate Armor
	 * 
	 * @return Item
	 */
	public static Item createPlateArmor() {
		Consumer<Context> consumerForBeforeTurn = context -> {
			var hero = context.hero();
			var shield = shieldAfterBoost(8, context);
			hero.addShield(shield);
			Effect.addEffect(hero, EffectType.SLOW, 1);
		};
		boolean[][] s = { { true, true }, { true, true } };
		return new Item("PlateArmor", ItemType.ARMOR, Rarity.RARE, new Shape(s, 2, 2, Direction.UP), false, false,
				-1, null, consumerForBeforeTurn);
	}

	/**
	 * Creates a Leather Boots
	 * 
	 * @return Item
	 */
	public static Item createLeatherBoots() {
		Consumer<Context> consumerForBeforeTurn = context -> {
			var item = context.itemSource();
			var pos = item.getPos();
			var shield = context.backpack().countRowInDirection(pos, item.getShape(), Direction.UP);
			shield = shieldAfterBoost(shield, context);
			context.hero().addShield(shield);
		};
		boolean[][] s = { { true, true } };
		return new Item("LeatherBoots", ItemType.ARMOR, Rarity.COMMON, new Shape(s, 2, 1, Direction.UP), false,
				false, -1, null, consumerForBeforeTurn);
	}

	/********************************* Mana *******************************/

	/**
	 * Creates a Manastone
	 * 
	 * @return Item
	 */
	public static Item createMana() {
		return new Item("Manastone", ItemType.MANASTONE, Rarity.COMMON, Shape.getA1By1Shape(), false, false, -1,
				null, null);
	}

	/******************************** Wand ********************************/

	/**
	 * Creates a Skull Wand
	 * 
	 * @return Item
	 */
	public static Item createSkullWand() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			if (hero.reduceMana(2)) {
				context.monsters().forEach(m -> {
					var damage = damageDealsAfterBoost(5, context);
					m.takeDamage(damage);
				});
				setLastMoveAndUpdateMonsters(TypeOfMoves.ATTACK, context);
			}
		};
		boolean[][] s = { { false, true }, { true, false } };
		return new Item("SkullWand", ItemType.WEAPON, Rarity.RARE, new Shape(s, 2, 2, Direction.UP), true, true, -1,
				consumerForOnUse, null);
	}

	/******************************* Consumable **************************/
	/**
	 * Creates a Meal
	 * 
	 * @return Item
	 */
	public static Item createMeal() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			hero.addEnergy(2);
			context.itemSource().reduceNumberOfUsesBy1();
		};
		boolean[][] s = { { true, true } };
		return new Item("Meal", ItemType.CONSUMABLE, Rarity.COMMON, new Shape(s, 2, 1, Direction.UP), true, false,
				2, consumerForOnUse, null);
	}

	/**
	 * Creates a Rare Herb
	 * 
	 * @return Item
	 */
	public static Item createRareHerb() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			hero.increaseMaxHp(3);
			context.itemSource().reduceNumberOfUsesBy1();
		};
		return new Item("RareHerb", ItemType.CONSUMABLE, Rarity.RARE, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/**
	 * Creates a Tea
	 * 
	 * @return Item
	 */
	public static Item createTea() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			Effect.addEffect(hero, EffectType.DODGE, 2);
			Effect.addEffect(hero, EffectType.REGEN, 2);
			context.itemSource().reduceNumberOfUsesBy1();
		};
		return new Item("Tea", ItemType.CONSUMABLE, Rarity.UNCOMMON, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/**
	 * Creates a Smelt
	 * 
	 * @return Item
	 */
	public static Item createSmelt() {
		Consumer<Context> consumerForOnUse = context -> {
			var hero = context.hero();
			Effect.addEffect(hero, EffectType.HASTE, 8);
			context.itemSource().reduceNumberOfUsesBy1();
		};
		return new Item("Smelt", ItemType.CONSUMABLE, Rarity.UNCOMMON, Shape.getA1By1Shape(), true, false, 1,
				consumerForOnUse, null);
	}

	/***************************************** Keys ******************************/

	public static Item createKey() {
		return new Item("Key", ItemType.KEY, Rarity.COMMON, Shape.getA1By1Shape(), false, false, -1, null, null);
	}

	/***************************************** Curse ******************************/

	public static Item createCurse() {
		boolean[][] shapeGrid = {
			{true, true, false},
			{false, true, true}
		};
		return new Item("Curse", ItemType.CURSE, Rarity.COMMON, 
				new Shape(shapeGrid, 2, 3, Direction.UP),
				false, false, -1, null, null);
	}
}