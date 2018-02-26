package org.silentpom.javafx.filters;

import org.opencv.core.Rect;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

/**
 * Created by Vlad on 24.02.2018.
 */
public class RectFilterTest {

    @Test
    public void testFilterElem() throws Exception {
        RectFilter filter = new RectFilter(0.5, 0.3, 0.1, 0.1);
        List<Rect> list = IntStream.range(0, 10)
                .mapToObj(i -> new Rect(i, i, (i+1) * 10, (i+1) * 20))
                .map(rect -> filter.filterElem(rect))
                .limit(6)
                .collect(Collectors.toList());

        assertEquals(list.get(0).x, 0);
        assertEquals(list.get(1).x, 1);
        assertEquals(list.get(2).x, 2);
        assertEquals(list.get(3).width, (int)(40 * 0.5 + 30 * 0.3 + 20 * 0.1 + 10 * 0.1 + 0.5));
    }

}