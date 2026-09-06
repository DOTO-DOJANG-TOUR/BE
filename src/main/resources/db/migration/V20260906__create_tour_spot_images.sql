CREATE TABLE tour_spot_images (
    tour_spot_image_id BIGINT NOT NULL,
    tour_spot_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    thumbnail_url TEXT,
    image_name VARCHAR(255),
    serial_number INTEGER,
    CONSTRAINT pk_tour_spot_images PRIMARY KEY (tour_spot_image_id),
    CONSTRAINT fk_tour_spot_images_tour_spot FOREIGN KEY (tour_spot_id) REFERENCES tour_spots (tour_spot_id)
);

CREATE INDEX idx_tour_spot_images_tour_spot_id ON tour_spot_images (tour_spot_id);
