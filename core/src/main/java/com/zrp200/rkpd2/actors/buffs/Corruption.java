/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.mobs.DwarfKing;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class Corruption extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	private float buildToDamage = 0f;
	
	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			target.alignment = Char.Alignment.ALLY;
			if(target instanceof DwarfKing.Subject) { // DK logic
				new DwarfKing().yell(Messages.get(DwarfKing.class,"corrupted",target.name()));
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean act() {
		buildToDamage += target.HT/200f;

		int damage = (int)buildToDamage;
		buildToDamage -= damage;

		if (damage > 0)
			target.damage(damage, this);

		spend(TICK);

		return true;
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add( CharSprite.State.DARKENED );
		else if (target.invisible == 0) target.sprite.remove( CharSprite.State.DARKENED );
	}

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}
