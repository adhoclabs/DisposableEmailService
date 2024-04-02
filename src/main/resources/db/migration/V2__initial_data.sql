INSERT INTO albums (
    id,
    title,
    artists,
    genre,
    created_at,
    updated_at
) VALUES(
            'f47ac10b-58cc-4372-a567-0e02b2c3d479',
            'Rock Album 1',
            ARRAY['Artist1'],
            'Rock',
            '2024-01-22 01:00:00',
            '2024-01-22 01:00:00'
        );

INSERT INTO songs (
    id,
    title,
    album_id,
    album_position,
    created_at,
    updated_at
) VALUES (
            'e47ac10b-58cc-4372-a567-0e02b2c3d478',
            'Song1',
            'f47ac10b-58cc-4372-a567-0e02b2c3d479',
            1,
            '2024-01-22 01:00:00',
            '2024-01-22 01:00:00'
        ),
        (
            'f47ac10b-58cc-4372-a567-0e02b2c3d479',
            'Song2',
            'f47ac10b-58cc-4372-a567-0e02b2c3d479',
            2,
            '2024-01-22 01:00:00',
            '2024-01-22 01:00:00'
        );
