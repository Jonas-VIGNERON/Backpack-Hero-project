package affichage.EndScreen;

import java.awt.Color;
import java.awt.Font;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import com.github.forax.zen.ScreenInfo;

public class EndScreen {

    private static final int BTN_W = 200;
    private static final int BTN_H = 60;
    private static final int BTN_X = 400;
    private static final int BTN_Y = 400;

    public static String askPlayerName() {
        var data = new EndScreenData();
        Application.run(Color.BLACK, context -> {
            while (!data.isFinished()) {
                Event event = context.pollOrWaitEvent(10);
                while (event != null) {
                    switch (event) {
                        case KeyboardEvent ke -> handleKeyboard(ke, data);
                        case PointerEvent pe -> handlePointer(pe, data, context.getScreenInfo());
                    }
                    event = context.pollEvent();
                }
                render(context, data);
            }
            context.dispose();
        });
        return data.getName();
    }

    private static void handleKeyboard(KeyboardEvent ke, EndScreenData data) {
        if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) return;
        switch (ke.key()) {
            case KeyboardEvent.Key.LEFT : data.removeLastChar();
            case KeyboardEvent.Key.UNDEFINED : break ;
            case KeyboardEvent.Key.CTRL : break;
            case KeyboardEvent.Key.ESCAPE : break;
            default : {
                var c = ke.key().name().charAt(0);
                if (Character.isLetterOrDigit(c) || c == ' ') data.addChar(c);
            }
        }
    }

    private static void handlePointer(PointerEvent pe, EndScreenData data, ScreenInfo info) {
        if (pe.action() != PointerEvent.Action.POINTER_DOWN) return;
        var mx = pe.location().x();
        var my = pe.location().y();
        if (mx >= BTN_X && mx <= BTN_X + BTN_W && my >= BTN_Y && my <= BTN_Y + BTN_H) {
            data.finish();
        }
    }

    private static void render(ApplicationContext context, EndScreenData data) {
        context.renderFrame(g -> {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Entrez votre pseudo : " + data.getName(), 200, 200);
            g.drawString("Press the left arrow to delete the last character.", 200, 260);

            g.setColor(Color.GREEN);
            g.fillRect(BTN_X, BTN_Y, BTN_W, BTN_H);
            g.setColor(Color.WHITE);
            g.drawRect(BTN_X, BTN_Y, BTN_W, BTN_H);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            var fm = g.getFontMetrics();
            var btnText = "Valider";
            var textX = BTN_X + (BTN_W - fm.stringWidth(btnText)) / 2;
            var textY = BTN_Y + (BTN_H - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(btnText, textX, textY);
        });
    }
}
