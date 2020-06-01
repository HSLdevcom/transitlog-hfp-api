ALTER TABLE otherevent ALTER tst TYPE timestamptz USING tst AT TIME ZONE 'UTC';
ALTER TABLE otherevent ALTER received_at TYPE timestamptz USING received_at AT TIME ZONE 'UTC';

ALTER TABLE unsignedevent ALTER tst TYPE timestamptz USING tst AT TIME ZONE 'UTC';
ALTER TABLE unsignedevent ALTER received_at TYPE timestamptz USING received_at AT TIME ZONE 'UTC';

ALTER TABLE lightpriorityevent ALTER tst TYPE timestamptz USING tst AT TIME ZONE 'UTC';
ALTER TABLE lightpriorityevent ALTER received_at TYPE timestamptz USING received_at AT TIME ZONE 'UTC';

ALTER TABLE stopevent ALTER tst TYPE timestamptz USING tst AT TIME ZONE 'UTC';
ALTER TABLE stopevent ALTER received_at TYPE timestamptz USING received_at AT TIME ZONE 'UTC';

ALTER TABLE vehicleposition ALTER tst TYPE timestamptz USING tst AT TIME ZONE 'UTC';
ALTER TABLE vehicleposition ALTER received_at TYPE timestamptz USING received_at AT TIME ZONE 'UTC';
