create type genre as enum(
    'Rock',
    'Hip Hop',
    'Classical',
    'Pop'
);

create table if not exists songs (
    id text primary key,
    title text,
    genre genre
);
