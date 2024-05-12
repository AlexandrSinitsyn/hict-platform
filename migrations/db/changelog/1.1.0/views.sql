--liquibase formatted sql

--changeset AlexSin:8
--Views

create table contact_map_views
(
    contact_map_id bigint                   not null,
    count          bigint                   not null check ( count >= 0 ),
    last_seen      timestamp with time zone not null default now(),
    primary key (contact_map_id),
    unique (contact_map_id),
    foreign key (contact_map_id) references contact_maps (contact_map_id)
);

create index contact_map_views_by_id on contact_map_views using hash (contact_map_id);

create function update_last_seen_contact_map() returns trigger as
$$
begin
    new.last_seen := now();
    return new;
end
$$ language plpgsql;

create trigger update_last_seen before insert or update
    on contact_map_views
    for each row execute procedure update_last_seen_contact_map();

--rollback drop index contact_map_by_id
--rollback drop function update_last_seen_contact_map
--rollback drop trigger update_last_seen
--rollback drop table contact_map_views
