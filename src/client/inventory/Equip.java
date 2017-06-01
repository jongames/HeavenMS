/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.inventory;

import client.MapleClient;
import constants.ServerConstants;
import constants.ExpTable;
import java.util.LinkedList;
import java.util.List;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;

public class Equip extends Item {

    public static enum ScrollResult {

        FAIL(0), SUCCESS(1), CURSE(2);
        private int value = -1;

        private ScrollResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    private byte upgradeSlots;
    private byte level, flag, itemLevel;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
    private float itemExp;
    private int ringid = -1;
    private boolean wear = false;
    private boolean isElemental = false;    // timeless or reverse

    public Equip(int id, short position) {
        this(id, position, 0);
    }

    public Equip(int id, short position, int slots) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;
        
        String itemName = MapleItemInformationProvider.getInstance().getName(id);
        if(itemName != null) this.isElemental = (itemName.contains("Timeless") || itemName.contains("Reverse"));
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.flag = flag;
        ret.vicious = vicious;
        ret.upgradeSlots = upgradeSlots;
        ret.itemLevel = itemLevel;
        ret.itemExp = itemExp;
        ret.level = level;
        ret.log = new LinkedList<>(log);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.setGiftFrom(getGiftFrom());
        return ret;
    }

    @Override
    public byte getFlag() {
        return flag;
    }

    @Override
    public byte getType() {
        return 1;
    }

    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public short getStr() {
        return str;
    }

    public short getDex() {
        return dex;
    }

    public short getInt() {
        return _int;
    }

    public short getLuk() {
        return luk;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getWatk() {
        return watk;
    }

    public short getMatk() {
        return matk;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public short getHands() {
        return hands;
    }

    public short getSpeed() {
        return speed;
    }

    public short getJump() {
        return jump;
    }

    public short getVicious() {
        return vicious;
    }

    @Override
    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setVicious(short vicious) {
        this.vicious = vicious;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    private int getStatModifier(boolean isAttribute) {
        if(ServerConstants.USE_EQUIPMNT_LVLUP_POWER) {
            if(isAttribute) return 2;
            else return 4;
        }
        else {
            if(isAttribute) return 4;
            else return 16;
        }
    }
    
    private void getUnitStatUpgrade(List<Pair<String, Integer>> stats, String name, int curStat, boolean isAttribute) {
        int maxUpgrade = Randomizer.rand(0, 1 + (curStat / getStatModifier(isAttribute)));
        if(maxUpgrade == 0) return;
            
        stats.add(new Pair<>(name, maxUpgrade));  // each 4 stat point grants a bonus stat upgrade on equip level up.
    }
    
    private void getUnitSlotUpgrade(List<Pair<String, Integer>> stats, String name) {
        if(Math.random() < 0.1) {
            stats.add(new Pair<>(name, 1));  // 10% success on getting a slot upgrade.
        }
    }
    
    private void improveDefaultStats(List<Pair<String, Integer>> stats) {
        if(dex > 0) getUnitStatUpgrade(stats, "incDEX", dex, true);
        if(str > 0) getUnitStatUpgrade(stats, "incSTR", str, true);
        if(_int > 0) getUnitStatUpgrade(stats, "incINT",_int, true);
        if(luk > 0) getUnitStatUpgrade(stats, "incLUK", luk, true);
        if(hp > 0) getUnitStatUpgrade(stats, "incMHP", hp, false);
        if(mp > 0) getUnitStatUpgrade(stats, "incMMP", mp, false);
        if(watk > 0) getUnitStatUpgrade(stats, "incPAD", watk, false);
        if(matk > 0) getUnitStatUpgrade(stats, "incMAD", matk, false);
        if(wdef > 0) getUnitStatUpgrade(stats, "incPDD", wdef, false);
        if(mdef > 0) getUnitStatUpgrade(stats, "incMDD", mdef, false);
        if(avoid > 0) getUnitStatUpgrade(stats, "incEVA", avoid, false);
        if(acc > 0) getUnitStatUpgrade(stats, "incACC", acc, false);
        if(speed > 0) getUnitStatUpgrade(stats, "incSpeed", speed, false);
        if(jump > 0) getUnitStatUpgrade(stats, "incJump", jump, false);
    }
    
    public void gainLevel(MapleClient c) {
        List<Pair<String, Integer>> stats = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel);
        if(stats.isEmpty()) improveDefaultStats(stats);
        
        if(ServerConstants.USE_EQUIPMNT_LVLUP_SLOTS) {
            getUnitSlotUpgrade(stats, "incVicious");
            getUnitSlotUpgrade(stats, "incSlot");
        }
        
        itemLevel++;
        if(ServerConstants.USE_DEBUG) c.getPlayer().dropMessage(6, "'" + MapleItemInformationProvider.getInstance().getName(this.getItemId()) + "' has LEVELED UP to level " + itemLevel + "!");
        
        for (Pair<String, Integer> stat : stats) {
            switch (stat.getLeft()) {
                case "incDEX":
                    dex += stat.getRight();
                    break;
                case "incSTR":
                    str += stat.getRight();
                    break;
                case "incINT":
                    _int += stat.getRight();
                    break;
                case "incLUK":
                    luk += stat.getRight();
                    break;
                case "incMHP":
                    hp += stat.getRight();
                    break;
                case "incMMP":
                    mp += stat.getRight();
                    break;
                case "incPAD":
                    watk += stat.getRight();
                    break;
                case "incMAD":
                    matk += stat.getRight();
                    break;
                case "incPDD":
                    wdef += stat.getRight();
                    break;
                case "incMDD":
                    mdef += stat.getRight();
                    break;
                case "incEVA":
                    avoid += stat.getRight();
                    break;
                case "incACC":
                    acc += stat.getRight();
                    break;
                case "incSpeed":
                    speed += stat.getRight();
                    break;
                case "incJump":
                    jump += stat.getRight();
                    break;
                    
                case "incVicious":
                    if(vicious > 0) {
                        vicious -= stat.getRight();
                        if(vicious < 0) vicious = 0;
                        
                        c.getPlayer().dropMessage(6, "A new Vicious Hammer opportunity has been found on the '" + MapleItemInformationProvider.getInstance().getName(getItemId()) + "'!");
                    }
                    
                    break;
                case "incSlot":
                    upgradeSlots += stat.getRight();
                    c.getPlayer().dropMessage(6, "A new upgrade slot has been found on the '" + MapleItemInformationProvider.getInstance().getName(getItemId()) + "'!");
                    break;
            }
        }
        c.announce(MaplePacketCreator.showEquipmentLevelUp());
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeignEffect(c.getPlayer().getId(), 15));
        c.getPlayer().forceUpdateItem(this);
    }

    public int getItemExp() {
        return (int) itemExp;
    }
    
    private double normalizedMasteryExp(int reqLevel) {
        return Math.max((2622.71 * Math.exp(reqLevel * 0.0533649)) - 6000.0, 15);
    }
    
    public void gainItemExp(MapleClient c, int gain) {  // Ronan's Equip Exp gain method
        if(itemLevel >= 30) return;
        
        int reqLevel = MapleItemInformationProvider.getInstance().getEquipStats(this.getItemId()).get("reqLevel");
        
        float masteryModifier = (float)ExpTable.getExpNeededForLevel(1) / (float)normalizedMasteryExp(reqLevel);
        float elementModifier = (isElemental) ? 1.2f : 1.0f;
        
        float baseExpGain = gain * elementModifier * masteryModifier;
        
        itemExp += baseExpGain;
        int expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);
        
        //System.out.println("Gain: " + gain + " Mastery: " + masteryModifier + "Base gain: " + baseExpGain + " item current exp: " + itemExp + " / " + expNeeded);
        
        if (itemExp >= expNeeded) {
            while(itemExp >= expNeeded) {
                itemExp = (itemExp - expNeeded);
                gainLevel(c);

                if(itemLevel == ServerConstants.USE_EQUIPMNT_LVLUP) {
                    itemExp = 0.0f;
                    break;
                }
                
                expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);
            }
        } else {
            c.getPlayer().forceUpdateItem(this);
            //if(ServerConstants.USE_DEBUG) c.getPlayer().dropMessage("'" + MapleItemInformationProvider.getInstance().getName(this.getItemId()) + "': " + itemExp + " / " + expNeeded);
        }
    }

    public void setItemExp(int exp) {
        this.itemExp = exp;
    }

    public void setItemLevel(byte level) {
        this.itemLevel = level;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setUpgradeSlots(int i) {
        this.upgradeSlots = (byte) i;
    }

    public void setVicious(int i) {
        this.vicious = (short) i;
    }

    public int getRingId() {
        return ringid;
    }

    public void setRingId(int id) {
        this.ringid = id;
    }

    public boolean isWearing() {
        return wear;
    }

    public void wear(boolean yes) {
        wear = yes;
    }

    public byte getItemLevel() {
        return itemLevel;
    }
}