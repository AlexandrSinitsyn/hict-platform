--liquibase formatted sql

--changeset AlexSin:4
--Views

create table hi_c_map_views
(
    hi_c_map_id bigint                   not null,
    count       bigint                   not null check ( count >= 0 ),
    last_seen   timestamp with time zone not null default now(),
    primary key (hi_c_map_id),
    constraint fk_hi_c_map_id foreign key (hi_c_map_id) references hi_c_maps (hi_c_map_id)
);

create index hi_c_map_views_by_id on hi_c_map_views using hash (hi_c_map_id) ;

create function update_last_seen_hi_c_map() returns trigger as
$$
begin
    new.last_seen := now();
    return new;
end
$$ language plpgsql ;

create trigger update_last_seen before insert or update
    on hi_c_map_views
    for each row execute procedure update_last_seen_hi_c_map() ;

--rollback drop index hi_c_map_by_id
--rollback drop index hi_c_map_by_name
--rollback drop index hi_c_map_by_author
--rollback truncate table hi_c_maps
--rollback drop trigger update_last_seen
--rollback drop function update_last_seen_hi_c_map
