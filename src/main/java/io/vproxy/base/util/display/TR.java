package io.vproxy.base.util.display;

import java.util.ArrayList;
import java.util.List;

public class TR {
    final List<String> columns = new ArrayList<>();

    public TR td(String col) {
        columns.add(col);
        return this;
    }
}
