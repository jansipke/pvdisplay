package nl.jansipke.pvdisplay.data;

public class AxisLabelValues {

    private final float max;
    private final float step;
    private final float view;

    public AxisLabelValues(float max, float step, float view) {
        this.max = max;
        this.step = step;
        this.view = view;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    public float getView() {
        return view;
    }
}
