create table posts
(
    id          serial primary key,
    title       varchar(255),
    link        varchar(255),
    description varchar,
    created     timestamp
);