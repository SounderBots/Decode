package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DelegateOrVoidTelemetry implements Telemetry {

    private final Telemetry _telemetry;
    private boolean delegate = true;

    public DelegateOrVoidTelemetry(Telemetry _telemetry, boolean delegate) {
        this._telemetry = _telemetry;
        this.delegate = delegate;
    }

    @Override
    public Item addData(String caption, String format, Object... args) {
        if (delegate) {
            return _telemetry.addData(caption, format, args);
        }
        return null;
    }

    @Override
    public Item addData(String caption, Object value) {
        if (delegate) {
            return _telemetry.addData(caption, value);
        }
        return null;
    }

    @Override
    public <T> Item addData(String caption, Func<T> valueProducer) {
        if (delegate) {
            return _telemetry.addData(caption, valueProducer);
        }
        return null;
    }

    @Override
    public <T> Item addData(String caption, String format, Func<T> valueProducer) {
        if (delegate) {
            return _telemetry.addData(caption, format, valueProducer);
        }
        return null;
    }

    @Override
    public boolean removeItem(Item item) {
        if (delegate) {
            return _telemetry.removeItem(item);
        }
        return false;
    }

    @Override
    public void clear() {
        if (delegate) {
            _telemetry.clear();
        }
    }

    @Override
    public void clearAll() {
        if (delegate) {
            _telemetry.clearAll();
        }
    }

    @Override
    public Object addAction(Runnable action) {
        if (delegate) {
            return _telemetry.addAction(action);
        }
        return null;
    }

    @Override
    public boolean removeAction(Object token) {
        if (delegate) {
            return _telemetry.removeAction(token);
        }
        return false;
    }

    @Override
    public void speak(String text) {
        if (delegate) {
            _telemetry.speak(text);
        }
    }

    @Override
    public void speak(String text, String languageCode, String countryCode) {
        if (delegate) {
            _telemetry.speak(text, languageCode, countryCode);
        }
    }

    @Override
    public boolean update() {
        if (delegate) {
            _telemetry.update();
        }
        return false;
    }

    @Override
    public Line addLine() {
        if (delegate) {
            return _telemetry.addLine();
        }
        return null;
    }

    @Override
    public Line addLine(String lineCaption) {
        if (delegate) {
            return _telemetry.addLine(lineCaption);
        }
        return null;
    }

    @Override
    public boolean removeLine(Line line) {
        if (delegate) {
            _telemetry.removeLine(line);
        }
        return false;
    }

    @Override
    public boolean isAutoClear() {
        if (delegate) {
            return _telemetry.isAutoClear();
        }
        return false;
    }

    @Override
    public void setAutoClear(boolean autoClear) {
        if (delegate) {
            _telemetry.setAutoClear(autoClear);
        }
    }

    @Override
    public int getMsTransmissionInterval() {
        if (delegate) {
            return _telemetry.getMsTransmissionInterval();
        }
        return 0;
    }

    @Override
    public void setMsTransmissionInterval(int msTransmissionInterval) {
        if (delegate) {
            _telemetry.setMsTransmissionInterval(msTransmissionInterval);
        }
    }

    @Override
    public String getItemSeparator() {
        if (delegate) {
            return _telemetry.getItemSeparator();
        }
        return "";
    }

    @Override
    public void setItemSeparator(String itemSeparator) {
        if (delegate) {
            _telemetry.setItemSeparator(itemSeparator);
        }
    }

    @Override
    public String getCaptionValueSeparator() {
        if (delegate) {
            return _telemetry.getCaptionValueSeparator();
        }
        return "";
    }

    @Override
    public void setCaptionValueSeparator(String captionValueSeparator) {
        if (delegate) {
            _telemetry.setCaptionValueSeparator(captionValueSeparator);
        }
    }

    @Override
    public void setDisplayFormat(DisplayFormat displayFormat) {
        if (delegate) {
            _telemetry.setDisplayFormat(displayFormat);
        }
    }

    @Override
    public Log log() {
        if (delegate) {
            _telemetry.log();
        }
        return null;
    }
}
