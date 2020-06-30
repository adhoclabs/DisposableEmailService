create type genre as enum(
    'NoGenre',
    'Rock',
    'HipHop',
    'Classical',
    'Pop'
);

create table if not exists albums (
    id uuid primary key,
    title text,
    artists text[],
    genre genre,
    created_at timestamp,
    updated_at timestamp
);

create table if not exists songs (
    id uuid primary key,
    title text,
    album_id uuid references albums(id) on delete cascade,
    album_position integer,
    created_at timestamp,
    updated_at timestamp
);
