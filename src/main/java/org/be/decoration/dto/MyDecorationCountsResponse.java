// org/be/decoration/dto/MyDecorationCountsResponse.java
package org.be.decoration.dto;

import java.util.Map;

public record MyDecorationCountsResponse(
        Map<Integer, Integer> decorationCounts // {1:개수, 2:개수, 3:개수}
) {}