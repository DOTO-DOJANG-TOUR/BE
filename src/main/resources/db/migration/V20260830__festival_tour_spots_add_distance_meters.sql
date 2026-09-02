ALTER TABLE festival_tour_spots
    ADD COLUMN distance_meters NUMERIC(10, 1);

UPDATE festival_tour_spots festival_tour_spot
SET distance_meters = ST_Distance(festival.location, tour_spot.location)
FROM festivals festival, tour_spots tour_spot
WHERE festival.festival_id = festival_tour_spot.festival_id
  AND tour_spot.tour_spot_id = festival_tour_spot.tour_spot_id;
