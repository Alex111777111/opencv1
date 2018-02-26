package org.silentpom.javafx.filters;

import org.opencv.core.Rect;

/**
 * Created by Vlad on 24.02.2018.
 */
public class RectFilter extends BasicFiniteFilter<Rect> {
    public RectFilter(double... coefs) {
        super(coefs);
    }

    public Rect filterElem(Rect t) {
        push(t);

        double[] filter = cutFilter();
        double x = filter( rect -> rect.x, filter);
        double y = filter( rect -> rect.y, filter);
        double w = filter( rect -> rect.width, filter);
        double h = filter( rect -> rect.height,filter);

        return new Rect((int) (x + 0.5), (int) (y + 0.5), (int) (w + 0.5), (int) (h + 0.5));
    }

    public Rect filterLost() {
        if(isEmpty()) {
            return null;
        }

        double[] filter = cutFilter();
        double x = filter( rect -> rect.x, filter);
        double y = filter( rect -> rect.y, filter);
        double w = filter( rect -> rect.width, filter);
        double h = filter( rect -> rect.height,filter);

        Rect t = new Rect((int) (x + 0.5), (int) (y + 0.5), (int) (w + 0.5), (int) (h + 0.5));
        push(t);

        return t;
    }

}
