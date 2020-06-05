create type genre as enum(
    'Rock',
    'HipHop',
    'Classical',
    'Pop'
);

create table if not exists albums (
    id uuid primary key,
    title text,
    genre genre
);

create table if not exists songs (
    id uuid primary key,
    title text,
    album_id uuid references albums(id) on delete cascade,
    album_position integer
);
