package se.kamikaze;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
public class Kamikaze_90 extends AdvancedRobot {
    public static final int[] KAMIKAZE_FINAL_PLAN = {10, 11, 12}; //Prioritetsordning för ScannadRobotEvent,
    public static final int[] KAMIKAZE_PLAN_A= {12, 10, 11};       //HitWallEvent och HitByBulletEvent i två olika situationer.
    public static boolean discover=false; // Innehåller värdet true när vi hittat en motståndare, får vi bestämma att attackera och följa, bara attackera, eller ignorera och kallar metoden move() istället för metoden fire() eller metoden fire_And_Follow().

    public static boolean rightUp;
    public static boolean leftUp; //De här variablerna behöver vi för att kunna avgöra
    public static boolean rightDown;//  i vilken del av kartan vår robot befinner sig.
    public static boolean leftDown;

    public static double bearingRadians;
    public static double eventDistance; // Dessa variabler använder vi för att kunna använda de
    private double heading = 0.0;       //  värden som finns i onScannadRobotEvent över hela klassen.
    private double radarHeading = 0.0;
    public static double eventBearing;

    public static int danger = 0; // Kan innehålla värden mellan 0 och 2, avgör risk nivå, ökar med 1 vid varje gång en kula träffar vår robot och blir 0 efter roboten utförde skyddsrörelser.
    public static  int firePower =3;// Kan innehålla värden mellan 1 och 3, avgör kulans styrka, ökar med 1 om den är mindre än 3 när vi lyckades och minskas med en om den är större än en när vi misslyckades.

    @Override
    public void run() {
        setBodyColor(Color.CYAN);
        setGunColor(Color.GREEN);
        setRadarColor(Color.GREEN);
        setBulletColor(Color.GREEN);
        setScanColor(Color.GREEN);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setTurnRadarLeft(Double.POSITIVE_INFINITY);
        while (true) {
            if(getOthers()>6){  // När motståndarna är mer än 6 vill vi inte följa efter
                discover=false; // någon motståndare utan bara utföra fire() metoden en gång.
            }
            myPosition();        // Avgör i vilken del av kartan vår robot befinner sig beroende på robotens nuvarande x, y, genom att tilldela dem värden, booleska variabler, vi får värden genom att jämföra robotens x med halvan av fältets bredd, samt jämföra robotens y med fältets höjd.
            if (discover)       //Kontrollera variabeln discover, om den har värdet true så betyder det att roboten skannade en motståndaren, samt att de roboter som är kvar är färre än 6 för att inte riskera att följa en robot när det är många som är kvar.
                doScan(eventBearing); // skanna mot den sidan som motståndaren befinner sig i, så här kan vi undvika välja till höger eller till vänster.
            turnRadarRightRadians(6.6);// Om vi inte lyckades följa motståndaren så skanna över kartan med 6,6 Radian varje while loop.
        }

    }

    public void onScannedRobot(ScannedRobotEvent event) {
        eventBearing = event.getBearing();
        eventDistance=event.getDistance();// Tilldela de viktiga värdena till statiska variabler så att vi kan använda dem över hela klassen då behöver vi dem i metoden follow ()
        bearingRadians=event.getBearingRadians();// som följer motståndaren beroende på variabler eventBearingRadian, och eventDistance samt använder vi dem i doScan () som skannar beroende på motståndarens placering.

        String klass = ScannedRobotEvent.class.getName().toString(); // Att ta reda på events klass namn så att vi kan ändra prioriteringsordning.
        if (getOthers()<9&&this.getEnergy()>event.getEnergy()) {
            if (event.getName() == "RoboCop" || event.getName() == "K4M1K4Z3") {

                //RoboCop och K4M1K4Z3 är de två farligaste motståndarna.
                /** Sätt den modiga Kamikazes slutliga plan i verket**/

                setEventPriority(klass, KAMIKAZE_FINAL_PLAN[0]); // Den högsta prioriteringen nu är att attackera och följa efter utan att ta hänsyn till andra skyddsåtgärder.
                fire_And_Follow();  /** Hunt them down **/
            }
        }
        if(getOthers()>6&&getOthers()<8){
            setEventPriority(klass,KAMIKAZE_PLAN_A[0]);
            fire();
        }
        else {
            setEventPriority(klass,KAMIKAZE_PLAN_A[0]);
            fire_And_Follow();
        }
    }
    private void doScan(double enemyGetBearing) {
        if (discover) {
            heading = this.getHeadingRadians();
            radarHeading = this.getRadarHeadingRadians();
            double temp = radarHeading - heading - enemyGetBearing;
            temp = Utils.normalRelativeAngle(temp);
            temp *= 1.2;
            setTurnRadarLeftRadians(temp);
            execute();
        }
    }
    public void  follow(double eventBearingRadians, double eventDistance){
        double radarTurn = getHeadingRadians() + eventBearingRadians - getRadarHeadingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
        double gunTurn = getHeadingRadians() + eventBearingRadians - getGunHeadingRadians();
        setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
        setTurnRight(eventBearing);
        setAhead(eventDistance +150);
    }
    public void fire_And_Follow()
    {
        fire(firePower);
        follow(bearingRadians,eventDistance);
    }
    public void fire(){
        if (eventDistance>20)
            fire(3);
        if (eventDistance<100){
            fire(firePower);
            if (rightUp||rightDown||leftDown||leftUp)
                goTo(500,500);
        }
        else if(eventDistance>299&& eventDistance<400){
            fire(2);
            if (rightUp||rightDown||leftDown||leftUp)
                goTo(500,500);
        }
        else{
            fire(1);
        }
        /**   turnRadarLeftRadians(6.6); **/ doScan(eventBearing);
    }
    @Override
    public void onHitWall(HitWallEvent event) {
        String klass = HitWallEvent.class.getName().toString();
        if(getOthers()>3){
            setEventPriority(klass,KAMIKAZE_PLAN_A[1]);
            move();
        }else {
            setEventPriority(klass, KAMIKAZE_FINAL_PLAN[1]);
            doScan(eventBearing);
        }
    }
    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        if(firePower>1)
            firePower--;
    }
    @Override
    public void onBulletHit(BulletHitEvent event) {
        if (firePower<3)
            firePower++;
    }
    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        String klass = HitByBulletEvent.class.getClass().toString();
        if(event.getName()=="RoboCop"||event.getName()=="K4M1K4Z3"){
            setEventPriority(klass,KAMIKAZE_FINAL_PLAN[2]);
        }
        if(getOthers()>3)
        {
            turnRight(20);
            back(500);
        }
        setEventPriority(klass,KAMIKAZE_PLAN_A[2]);
        if (getOthers()>2){
            move();
            danger++;
        }
        else if (getOthers()>4&&danger>=2){
            turnLeft(30);
            back(200);
            turnRight(40);
            ahead(200);
            move();
            danger=0;
        }else {
            goTo(500,500);
        }

    }
    private void goTo(double x, double y) {
        double a;
        setTurnRightRadians(Math.tan(
                a = Math.atan2(x -= (int) getX(), y -= (int) getY())
                        - getHeadingRadians()));
        setAhead(Math.hypot(x, y) * Math.cos(a));
    }
    public void myPosition(){
        rightDown=getX()-200>getBattleFieldWidth()/2&&getY()+400<getBattleFieldHeight()/2;
        rightUp=getX()-200>getBattleFieldWidth()/2&&getY()-200>getBattleFieldHeight()/2;
        leftDown=getX()+400<getBattleFieldWidth()/2&&getY()+400<getBattleFieldHeight()/2;
        leftUp=getX()+400<getBattleFieldWidth()/2&&getY()-200>getBattleFieldHeight()/2;
    }
    public void move(){
        double nextX;
        double nextY;
        if (leftUp||leftDown){
            nextX=getBattleFieldWidth()-100;
        }
        else {
            nextX = 100;
        }
        if(rightDown||leftDown) {
            nextY = getBattleFieldHeight() - 100;
        }
        else {
            nextY = 100;
        }
        turnLeft(40);
        ahead(70);
        goTo(nextX,nextY);
    }
}
