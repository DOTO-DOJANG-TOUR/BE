package com.doto.domain.tourex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * 한국관광공사 TourAPI 관광지 이미지 갤러리(detailImage2) 응답을 위한 외부 API 전용 DTO
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TourImageApiResponseDTO(
        Response response
) {

    public List<TourImageDTO> itemsOrEmpty() {
        if (response == null || response.body() == null || response.body().items() == null
                || response.body().items().item() == null) {
            return List.of();
        }
        return response.body().items().item();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Header header,
            Body body
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            String resultCode,
            String resultMsg
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonDeserialize(using = ItemsDeserializer.class)
            Items items,
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            List<TourImageDTO> item
    ) {
    }

    // TourAPI가 이미지가 없을 때 items를 빈 문자열 또는 배열로 내려주는 경우를 빈 목록으로 처리
    public static class ItemsDeserializer extends StdDeserializer<Items> {

        public ItemsDeserializer() {
            super(Items.class);
        }

        @Override
        public Items deserialize(JsonParser parser, tools.jackson.databind.DeserializationContext context)
                throws tools.jackson.core.JacksonException {
            JsonNode itemsNode = context.readTree(parser);
            if (itemsNode == null || itemsNode.isNull() || itemsNode.isArray()
                    || (itemsNode.isTextual() && itemsNode.asText().isBlank())) {
                return new Items(List.of());
            }

            JsonNode itemNode = itemsNode.get("item");
            if (itemNode == null || itemNode.isNull()) {
                return new Items(List.of());
            }
            if (itemNode.isArray()) {
                return new Items(Arrays.asList(context.readTreeAsValue(itemNode, TourImageDTO[].class)));
            }
            return new Items(List.of(context.readTreeAsValue(itemNode, TourImageDTO.class)));
        }
    }

    // 관광지 하나에 딸린 이미지 한 장의 정보
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TourImageDTO(
            @JsonProperty("contentid") Long contentId,
            @JsonProperty("originimgurl") String originImageUrl,
            @JsonProperty("smallimageurl") String smallImageUrl,
            @JsonProperty("imgname") String imageName,
            @JsonProperty("serialnum") String serialNumber
    ) {
    }
}
