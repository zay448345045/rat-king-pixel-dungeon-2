/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.BArray;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Dagger extends MeleeWeapon {
	
	{
		image = ItemSpriteSheet.DAGGER;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;

		tier = 1;
		
		bones = false;
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //8 base, down from 10
				lvl*(tier+1);   //scaling unchanged
	}

	protected float surpriseTowardMax = 0.75f;
	
	@Override
	public int damageRoll(Char owner) {
		if (owner instanceof Hero) {
			Hero hero = (Hero)owner;
			Char enemy = hero.enemy();
			if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
				//deals 75% toward max to max on surprise, instead of min to max.
				int diff = max() - min();
				int damage = augment.damageFactor(Random.NormalIntRange(
						min() + Math.round(diff* surpriseTowardMax),
						max()));
				int exStr = hero.STR() - STRReq();
				if (exStr > 0) {
					damage += Random.IntRange(0, exStr);
				}
				return damage;
			}
		}
		return super.damageRoll(owner);
	}

	@Override
	public float abilityChargeUse( Hero hero ) {
		return 2*super.abilityChargeUse(hero);
	}

	@Override
	public String targetingPrompt() {
		return Dungeon.hero.heroClass == HeroClass.DUELIST ? Messages.get(this, "prompt") :
				null;
	}

	// 8 dagger / 6 dirk
	// this is also the maximum invisibility time
	protected int maxDist() { return 10 - 2 * tier; }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null) {
			target = hero.pos;
		}

		Char ch = Actor.findChar(target);

		if ((ch != null && ch != hero) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(this, "ability_bad_position"));
			return;
		}

		PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.not(Dungeon.level.solid, null), maxDist());
		int distance = PathFinder.distance[target];
		if (distance == Integer.MAX_VALUE) {
			GLog.w(Messages.get(this, "ability_bad_position"));
			return;
		}

		beforeAbilityUsed(hero);
		// you can trade distance for longer invis if you want.
		Buff.affect(hero, Invisibility.class, Math.max(Actor.TICK, maxDist() - distance));
		hero.spendAndNext(Actor.TICK);

		if (ch == null) {
			Dungeon.hero.sprite.turnTo( Dungeon.hero.pos, target);
			Dungeon.hero.pos = target;
			Dungeon.level.occupyCell(Dungeon.hero);
			Dungeon.observe();
			GameScene.updateFog();
			Dungeon.hero.checkVisibleMobs();
			Dungeon.hero.sprite.place( Dungeon.hero.pos );
		}
		CellEmitter.get( Dungeon.hero.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
		Sample.INSTANCE.play( Assets.Sounds.PUFF );
		afterAbilityUsed(hero);
	}

}
