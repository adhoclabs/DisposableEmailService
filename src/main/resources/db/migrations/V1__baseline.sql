create type genre as enum(
    'Rock',
    'Hip Hop',
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
    album uuid references albums(id)
);
