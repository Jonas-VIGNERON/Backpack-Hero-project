package game.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import game.effect.EffectType;
import game.entity.monsters.InfoMonster;
import game.entity.monsters.moves.Attack;
import game.entity.monsters.moves.Curse;
import game.entity.monsters.moves.Dodge;
import game.entity.monsters.moves.Move;
import game.entity.monsters.moves.Poison;
import game.entity.monsters.moves.Shield;
import game.entity.monsters.moves.Summon;
import game.entity.monsters.moves.TypeOfMoves;
import game.item.ItemsCreation;
import game.random.Randomizer;
import game.utils.UtilsFunctions;

/**
 * Class which implements monsters.
 */
public final class Monster implements Target{
	private int hp;
	private int shield = 0;
	private List<Move> nextMoves;
	private ArrayList<EffectType> effects;
	
	private final InfoMonster infos;

	/**
	 * Monster's builder
	 * 
	 * @param name the monster's name
	 * @param hp the monster's hp
	 * @param xp the amount of xp that the monster gives when he dies
	 * @param moves the consumer that contains all the monster's possible moves.
	 */
	public Monster(String name, int hp, int xp, Consumer<Monster> moves) {
		Objects.requireNonNull(List.of(name, moves));
		if (hp < 0) {
			throw new IllegalArgumentException("hp must not be negative");
		}
		if (xp < 0) {
			throw new IllegalArgumentException("xp must not be negative");
		}
		this.hp = hp;
		nextMoves = List.of();
		effects = new ArrayList<>();
		infos = new InfoMonster(name, hp, xp, moves);
	}
	
	/*********************************Functions to get the monster's stats*****************/
	
	/**
	 * Getter for monster's HP.
	 * 
	 * @return int the monster's hp
	 */
	public int getHp() {
		return hp;
	}
	/**
	 * Getter for monster's Shield.
	 * 
	 * @return int the monster's shield
	 */
	public int getShield() {
		return shield;
	}
	
	/**
	 * Getter for monster's Infos, which contains monster's unmodifiable data
	 * 
	 * @return InfoMonster
	 */
	public InfoMonster getInfos() {
		return infos;
	}
	
	/**
	 * Getter for monster's nextMoves.
	 * 
	 * @return List<Move>
	 */
	public List<Move> getNextMoves(){
		return nextMoves;
	}
	
	/**
	 * Getter for monster's Effects
	 * 
	 * @return ArrayList<Effecttype>
	 */
	public ArrayList<EffectType> getEffects(){
		return effects;
	}

	/**
	 * Getter for monster's name
	 * @return
	 */
	public String getName() {
		return infos.name();
	}
	/************************************* Functions to modify stats ********************************/
	
	/**
	 * Deals <b>damage</b> damage to the targeted monster.
	 */
	public void takeDamage(int damage){
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		// First modify the damage deals by applying effects that can increase or decrease this amount
		damage = Target.damageTakenAfterApplyingEffects(this, damage);
		// Then check if the monster has a dodge, if so reduce the damage to 0.
		damage = Target.applyDodge(this, damage);
		// Reduce shield first and then hp if the amount of shield is not big enough to cover the damages.
		Target.takeDamage(damage, () -> shield, remainShield -> shield = remainShield, () -> hp, remainHp -> hp = remainHp);
	}
	
	/**
	 * Deals <b>damage</b> to the monster regardless his shield.
	 */
	public void takeDamageIgnoringShield(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		hp -= damage;
	}
	
	/**
	 * Updates a monster statistic using a functional approach.
	 * This method retrieves the current value of a stat using {@code getter},
	 * combines it with {@code plusStat} using the provided operation {@code f},
	 * and applies the result using {@code setter}.
	 *
	 * @param plusStat the value to combine with the current stat (e.g. amount to add)
	 * @param getter supplies the current value of the stat
	 * @param setter updates the stat with the computed result
	 * @param f a binary operation that defines how the stat is updated (e.g. addition, max, min, etc.)
	 *
	 */
	private void addStats(int plusStat, IntSupplier getter, IntConsumer setter, IntBinaryOperator f) {
		setter.accept(f.applyAsInt(getter.getAsInt(), plusStat));
	}
	
	/**
	 * Increase the monster's shield by {@code moreShield}
	 * 
	 * @param moreShield the additional amount of shield to give to the monster
	 */
	public void addShield(int moreShield) {
		if (moreShield < 0) {
			throw new IllegalArgumentException("moreShield must not be negative");
		}
		moreShield = Target.shieldAfterEffects(this, moreShield);
		addStats(moreShield, () -> shield, value -> shield = value, (s, v) -> s + v);
	}
	
	/**
	 * Add hp to this monster.
	 */
	public void addHp(int moreHp) {
		if (moreHp < 0) {
			throw new IllegalArgumentException("moreHp must not be negative");
		}
		addStats(moreHp, () -> hp, value -> hp = value, (h, v) -> (h + v) > infos.maxHp() ? infos.maxHp() : h + v);
	}
	
	/**
	 * Defines the monster's move(s)
	 */
	public void defineMoves() {
		infos.moves().accept(this);
	}
	
	/**
	 * Sets the monster's move(s)
	 * @param newMoves a list of moves
	 */
	public void setNextMoves(List<Move> newMoves) {
		Objects.requireNonNull(newMoves);
		nextMoves = newMoves;
	}
	
	/**
	 * A builder for the Little Rat Wolf
	 * 
	 * @return {@Monster} Here is a Little Rat Wolf
	 */
	private static Monster createLittleRatWolf() {
		Consumer<Monster> moves = monster -> {
			var numberOfMoves = Randomizer.random(1, 4);
			var listOfMoves = new ArrayList<Move>();
		    for (var i = 0; i < numberOfMoves; i++) {
		        if (Math.random() < 0.5) {
		            listOfMoves.add(new Attack(Randomizer.random(5, 7)));
		        } else {
		            listOfMoves.add(new Shield(10));
		        }
		    }
		    monster.setNextMoves(listOfMoves);
		};
		return new Monster("LittleRatWolf", 45, 6, moves);
	}
	
	/**
	 * A builder for the Rat Wolf
	 * 
	 * @return {@Monster} Here is a Rat Wolf
	 */
	private static Monster createRatWolf() {
		Consumer<Monster> moves = monster -> {
			var numberOfMoves = Randomizer.random(1, 4);
			var listOfMoves = new ArrayList<Move>();
		    for (var i = 0; i < numberOfMoves; i++) {
		    	double r = Math.random();
		        if (r < 0.2) {
		        	listOfMoves.add(new Curse(ItemsCreation.createCurse()));
		        } 
		        else if (r < 0.6) {
		            listOfMoves.add(new Attack(Randomizer.random(7, 9)));
		        } 
		        else {
		            listOfMoves.add(new Shield(Randomizer.random(11, 14)));
		        }
		    }
		    monster.setNextMoves(listOfMoves);
		};
		return new Monster("RatWolf", 45, 6, moves);
	}
	
	/**
	 * A builder for the Frog Wizard
	 * 
	 * @return {@Monster} Here is a Frog Wizard
	 */
	private static Monster createFrogWizard() {
		Consumer<Monster> moves = monster -> {
			var numberOfMoves = Randomizer.random(1, 4);
			var listOfMoves = new ArrayList<Move>();
			for(var i = 0; i < numberOfMoves; i++) {
				if (Math.random() < 0.2) {
					listOfMoves.add(new Curse(ItemsCreation.createCurse()));
				} else {
					listOfMoves.add(new Poison(4));
				}
			}
			monster.setNextMoves(listOfMoves);
		};
		return new Monster("FrogWizard", 60, 10, moves);
	}
	
	/**
	 * A builder for the Queen Bee
	 * 
	 * @return {@Monster} Here is a Queen Bee
	 */
	private static Monster createQueenBee() {
		Consumer<Monster> moves = monster -> {
			var numberOfMoves = Randomizer.random(1, 4);
			var listOfMoves = new ArrayList<Move>();
			for(var i = 0; i < numberOfMoves; i++) {
				var r = Math.random();
				if (r < 0.4) {
					listOfMoves.add(new Poison(2));
				} else if (r >= 0.4 && r <= 0.8){
					listOfMoves.add(new Attack(13));
				} else {
					listOfMoves.add(new Summon("LilBee"));
				}
			}
			monster.setNextMoves(listOfMoves);
		};
		return new Monster("QueenBee", 74, 20, moves);
	}
	
	/**
	 * A builder for the Lil Bee
	 * 
	 * @return {@Monster} Here is a Lil Bee
	 */
	private static Monster createLilBee() {
		Consumer<Monster> moves = monster -> {
			var numberOfMoves = Randomizer.random(1, 4);
			var listOfMoves = new ArrayList<Move>();
			for(var i = 0; i < numberOfMoves; i++) {
				var r = Math.random();
				if (r < 0.5) {
					listOfMoves.add(new Attack(Randomizer.random(5, 10)));
				} else {
					listOfMoves.add(new Shield(10));
				}
			}
			monster.setNextMoves(listOfMoves);
		};
		return new Monster("LilBee", 16, 4, moves);
	}
	
	/**
	 * Builder for the Living Shadow
	 * 
	 * @param hero the hero (useful here because this monster copy the last hero's move)
	 * @return {@Monster} Here is a Living Shadow
	 */
	private static Monster createLivingShadow(Hero hero) {
		Objects.requireNonNull(hero);
	    Consumer<Monster>  moves = monster -> {
	            var lastMove = hero.getLastMove();
	            Move move;
	            if (lastMove == TypeOfMoves.SHIELD) {
	                move = new Shield(Randomizer.random(9, 18));
	            } else if (lastMove == TypeOfMoves.ATTACK) {
	                move = new Attack(Randomizer.random(8, 20));
	            } else if (lastMove == TypeOfMoves.POISON) {
	            	move = new Poison(Randomizer.random(1,  6));
	            } else {
	            	move = new Dodge();
	            }
	            monster.setNextMoves(List.of(move));
	        };
	    return new Monster("LivingShadow", 40, 5, moves);
	}

	/**
	 * Create a monster depending on the {@code name}
	 * 
	 * @param name the monster's name
	 * @param hero the hero (used for the Living Shadow)
	 * @return a {@Monster}
	 */
	public static Monster createMonsterByName(String name, Hero hero) {
		UtilsFunctions.checkIfNonNull(List.of(name, hero));
		return switch(name) {
			case "LittleRatWolf" -> createLittleRatWolf(); 
			case "RatWolf" -> createRatWolf();
			case "FrogWizard" -> createFrogWizard();
			case "QueenBee" -> createQueenBee();
			case "LilBee" -> createLilBee();
			case "LivingShadow" -> createLivingShadow(hero);
			default -> throw new IllegalArgumentException("name of monster unknown");
		};
	}
	
	
	/***************** Functions that change the monster's stats before and after the turn***********/
	
	/**
	 * Resets the monster's shield
	 */
	public void initBeforeTurn() {
		shield = 0;
	}
	/**
	 * Removes all the effects apply to this monster.
	 */
	public void removeAllEffects() {
		effects = new ArrayList<>();
	}
	
	/********************************************************************************/
	
	/**
	 * Defines the amount of gold a Monster give when he dies
	 * 
	 * @return int the amount of gold.
	 */
	public int defineGoldWhenDead() {
		var monstersAndPrices = Map.of("RatWolf", 5, "LittleRatWolf", 3, "FrogWizard", 10,
				"QueenBee", 12, "LilBee", 1, "LivingShadow", 15);
		String name = getName();
		return monstersAndPrices.get(name);
	}

}
