package military.engine;

import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Nate
 */
public class CombatStats {

    private final Unit attacker;
    private final Unit defender;
    private int type;
    private int attackerBUA;
    private int attackerBUD;
    private int defenderBUA;
    private int defenderBUD;
    private int surround;
    private int attackerASup;
    private int attackerDSup;
    private int defenderASup;
    private int defenderDSup;
    private int attackerTerrain;
    private int defenderTerrain;
    private int attackerFA;
    private int attackerFD;
    private int defenderFA;
    private int defenderFD;
    private int attackerHB;
    private int attackerEB;
    private int defenderHB;
    private int defenderEB;

    public CombatStats(Unit attacker, Unit defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.calcType();
        this.before();
        this.calcBase();
        this.surround();
        this.support();
        this.calcFinal();
        this.calcLosses();
    }

    private void calcType() {
        if (attacker.isRanged()) {
            type = 3;
        }
        if (attacker.isAir()) {
            type = 6;
        }
        if (defender.isRanged()) {
            type += 1;
        }
        if (defender.isAir()) {
            type += 2;
        }
    }

    private void before() {
        attackerHB = attacker.getHealth();
        attackerEB = attacker.getExp();
        defenderHB = defender.getHealth();
        defenderEB = defender.getExp();
    }

    private void calcBase() {
        double expMod[] = {1.0, 1.05, 1.1, 1.2, 1.3, 1.4, 1.6, 1.8, 2.0};
        attackerBUA = (int) ((defender.isAir() ? attacker.getAirAttack()
                : attacker.getLandAttack()) * attacker.getHealth() * expMod[attacker.getExp()]);
        defenderBUA = (int) ((attacker.isAir() ? defender.getAirAttack()
                : defender.getLandAttack()) * defender.getHealth() * expMod[defender.getExp()]);
        if (attacker.isRanged()) {
            defenderBUA = 0;
        }
        attackerBUD = (int) (attacker.getDefense() * attacker.getHealth() * expMod[attacker.getExp()]);
        defenderBUD = (int) (defender.getDefense() * defender.getHealth() * expMod[defender.getExp()]);
    }

    private void surround() {
        ArrayList<Unit> surr = defender.getLoc().getAjacentUnits();
        surround = 0;
        for(Unit u : surr){
            if(u.getTeam() == attacker.getTeam()){
                surround++;
            }
        }
        if(!attacker.isRanged()){
            surround--;
        }
    }

    private void support() {
        ArrayList<Unit> dsup = defender.getLoc().getAjacentUnits();
        ArrayList<Unit> asup = attacker.getLoc().getAjacentUnits();
        attackerASup = 0;
        attackerDSup = 0;
        defenderASup = 0;
        defenderDSup = 0;
        for(Unit u: dsup){
            if(u.getTeam() == defender.getTeam()){
                defenderDSup  += u.getDefense()*u.getHealth()*.25;
            }
        }
//        for(Unit u: asup){
//            if(u.getTeam() == attacker.getTeam()){
//                if(defender.isAir()){
//                    attackerASup  += u.getAirAttack()*u.getHealth()*.25;
//                }
//                else{
//                    attackerASup  += u.getLandAttack()*u.getHealth()*.25;
//                }
//            }
//        }
    }

    private void calcFinal() {
        double surrMod[] = {1.0, 0.8, 0.7, 0.6, 0.5, 0.25};
        attackerTerrain = attacker.getLoc().getTerrain();
        defenderTerrain = defender.getLoc().getTerrain();
        attackerFA = (attackerBUA + attackerASup);
        attackerFD = ((attackerBUD + attackerDSup) * (attackerTerrain + 100) / 100);
        defenderFA = (int) ((defenderBUA + defenderASup) * surrMod[surround]);
        defenderFD = (int) ((defenderBUD + defenderDSup) * surrMod[surround] * (defenderTerrain + 100) / 100);
    }

    private void calcLosses() {
        Random rand = new Random();
        int baseAonD = (attackerFA - defenderFD);
        int baseDonA = (defenderFA - attackerFD);
        int damAonD = (int) ((2.5 * rand.nextDouble()) + (((double)baseAonD) / 50) * 1.2 * pow(rand.nextDouble(), .25));
        int damDonA = (int) ((2.5 * rand.nextDouble()) + (((double)baseDonA) / 50) * 1.2 * pow(rand.nextDouble(), .25));

        if (defenderBUA == 0) {
            damDonA = 0;
        }
        
        if(damAonD < 0){
            damAonD = 0;
        }
        if(damDonA < 0){
            damDonA = 0;
        }

        attacker.setHealth(attackerHB - damDonA);
        defender.setHealth(defenderHB - damAonD);
        
        
        if (attackerHB - damDonA > 0 && defenderHB - damAonD > 0) {
            attacker.addExp(1);
            defender.addExp(1);
        } else if (damAonD == 0) {
            defender.addExp(2);
        } else if (defenderHB - damAonD <= 0) {
            attacker.addExp(2);
        } else if (attackerHB - damDonA <= 0) {
            defender.addExp(1);
        }
    }

    public Unit getAttacker() {
        return attacker;
    }

    public Unit getDefender() {
        return defender;
    }

    public int getType() {
        return type;
    }

    public int getAttackerBUA() {
        return attackerBUA;
    }

    public int getAttackerBUD() {
        return attackerBUD;
    }

    public int getDefenderBUA() {
        return defenderBUA;
    }

    public int getDefenderBUD() {
        return defenderBUD;
    }

    public int getSurround() {
        return surround;
    }

    public int getAttackerASup() {
        return attackerASup;
    }

    public int getAttackerDSup() {
        return attackerDSup;
    }

    public int getDefenderASup() {
        return defenderASup;
    }

    public int getDefenderDSup() {
        return defenderDSup;
    }

    public int getAttackerTerrain() {
        return attackerTerrain;
    }

    public int getDefenderTerain() {
        return defenderTerrain;
    }

    public int getAttackerFA() {
        return attackerFA;
    }

    public int getAttackerFD() {
        return attackerFD;
    }

    public int getDefenderFA() {
        return defenderFA;
    }

    public int getDefenderFD() {
        return defenderFD;
    }

    public int getAttackerHB() {
        return attackerHB;
    }

    public int getAttackerEB() {
        return attackerEB;
    }

    public int getDefenderHB() {
        return defenderHB;
    }

    public int getDefenderEB() {
        return defenderEB;
    }

    @Override
    public String toString() {
        return "\n+=================================+" +
                "\nattacker=" + attacker +
                "\ndefender=" + defender +
                "\ntype=" + type +
                "\nattackerBUA=" + attackerBUA +
                "\nattackerBUD=" + attackerBUD +
                "\ndefenderBUA=" + defenderBUA +
                "\ndefenderBUD=" + defenderBUD +
                "\nsurround=" + surround +
                "\nattackerASup=" + attackerASup +
                "\nattackerDSup=" + attackerDSup +
                "\ndefenderASup=" + defenderASup +
                "\ndefenderDSup=" + defenderDSup +
                "\nattackerTerrain=" + attackerTerrain +
                "\ndefenderTerrain=" + defenderTerrain +
                "\nattackerFA=" + attackerFA +
                "\nattackerFD=" + attackerFD +
                "\ndefenderFA=" + defenderFA +
                "\ndefenderFD=" + defenderFD +
                "\nattackerHB=" + attackerHB +
                "\nattackerEB=" + attackerEB +
                "\ndefenderHB=" + defenderHB +
                "\ndefenderEB=" + defenderEB +
                "\n+=================================+";
    }

}
