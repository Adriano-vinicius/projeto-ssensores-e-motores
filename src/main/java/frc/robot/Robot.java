package frc.robot;

import com.studica.frc.MockDS;
import com.studica.frc.Titan;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.command.Scheduler;

public class Robot extends TimedRobot {

    private Titan titan;
    private Titan.Motor motor0;
    private Titan.Motor motor1;
    private Titan.Motor motor2;

    private MockDS ds;

    private DigitalInput btnStart;
    private DigitalInput btnStop;

    private Ultrasonic sonar;
    private boolean sonarOk = false;

    private boolean lastStart = false;
    private boolean lastStop = false;

    private Timer sonarPrintTimer = new Timer();
    private Timer manobraTimer = new Timer();

    private enum AutoState {
        ANDAR_FRENTE,
        RECUAR
    }

    private AutoState autoState = AutoState.ANDAR_FRENTE;

    @Override
    public void robotInit() {
        titan = new Titan(Constants.TITAN_ID);
        motor0 = titan.getMotor(Constants.MOTOR_0);
        motor1 = titan.getMotor(Constants.MOTOR_1);
        motor2 = titan.getMotor(Constants.MOTOR_2);

        ds = new MockDS();

        btnStart = new DigitalInput(Constants.BTN_START);
        btnStop = new DigitalInput(Constants.BTN_STOP);

        try {
            sonar = new Ultrasonic(Constants.ULTRASONIC_TRIGGER, Constants.ULTRASONIC_ECHO);

            // Modo manual: o código dispara o TRIG usando sonar.ping()
            sonar.setAutomaticMode(false);
            sonar.setEnabled(true);

            sonarOk = true;
            System.out.println("Ultrassonico iniciado com sucesso.");
        } catch (Exception e) {
            sonarOk = false;
            System.out.println("Erro ao iniciar o ultrassonico:");
            e.printStackTrace();
        }

        sonarPrintTimer.reset();
        sonarPrintTimer.start();

        stopMotors();
    }

    @Override
    public void robotPeriodic() {
        Scheduler.getInstance().run();

        boolean curStart = btnStart.get();
        boolean curStop = btnStop.get();

        if (lastStart && !curStart) {
            ds.enable();
            System.out.println("Comando: Habilitar enviado.");
        }

        if (lastStop && !curStop) {
            ds.disable();
            System.out.println("Comando: Desabilitar enviado.");
        }

        lastStart = curStart;
        lastStop = curStop;

        if (sonarOk && sonarPrintTimer.get() >= 0.5) {
            double distanciaCm = getDistanceCm();

            if (distanciaCm < 0) {
                System.out.println("Ultrassonico: sem leitura valida.");
            } else {
                System.out.printf("Ultrassonico: %.1f cm%n", distanciaCm);
            }

            sonarPrintTimer.reset();
        }
    }

    @Override
    public void autonomousInit() {
        autoState = AutoState.ANDAR_FRENTE;
        manobraTimer.stop();
        manobraTimer.reset();
    }

    @Override
    public void autonomousPeriodic() {
        if (!sonarOk) {
            andarFrente();
            return;
        }

        double distanciaCm = getDistanceCm();

        switch (autoState) {
            case ANDAR_FRENTE:
                andarFrente();

                if (distanciaCm > 0 && distanciaCm <= 6.0) {
                    autoState = AutoState.RECUAR;
                    manobraTimer.reset();
                    manobraTimer.start();
                    System.out.printf("Obstaculo detectado: %.1f cm -> recuando%n", distanciaCm);
                }
                break;

            case RECUAR:
                recuar();

                if (manobraTimer.get() >= 0.6) {
                    manobraTimer.stop();
                    manobraTimer.reset();
                    autoState = AutoState.ANDAR_FRENTE;
                    System.out.println("Voltando a andar.");
                }
                break;
        }
    }

    private void andarFrente() {
        motor0.set(0.0);
        motor1.set(Constants.VELOCIDADE * 0.5);
        motor2.set(-Constants.VELOCIDADE * 0.5);
    }

    private void recuar() {
        motor0.set(0.0);
        motor1.set(-Constants.VELOCIDADE * 0.5);
        motor2.set(Constants.VELOCIDADE * 0.5);
    }

    private double getDistanceCm() {
        if (!sonarOk || sonar == null) {
            return -1.0;
        }

        try {
            // Dispara o TRIG
            sonar.ping();

            // Espera o ECHO voltar
            Timer.delay(0.05);

            // Se não recebeu ECHO válido, retorna erro
            if (!sonar.isRangeValid()) {
                return -1.0;
            }

            return sonar.getRangeMM() / 10.0;

        } catch (Exception e) {
            System.out.println("Erro lendo ultrassonico:");
            e.printStackTrace();
            return -1.0;
        }
    }

    private void stopMotors() {
        motor0.set(0.0);
        motor1.set(0.0);
        motor2.set(0.0);
    }

    @Override
    public void disabledPeriodic() {
        stopMotors();
    }

    @Override
    public void disabledInit() {
        stopMotors();
    }
}