--All users
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('6eec88ff-e643-4617-9d77-10cba6044050', '2022-05-13 13:13:12.999998', null, true, null, 'mete@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, true, 'Mete');
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', '2022-05-13 13:13:12.999999', null, true, null, 'elvio@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, true, 'Elvio');
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', '2022-05-13 13:13:14.000001', null, true, null, 'jerome@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, true, 'Jerome');
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('f1961be8-fb94-4431-8262-eef1f71257fe', '2022-05-13 13:13:14.000002', null, true, null, 'matej@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, true, 'Matej');
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', '2022-05-13 13:13:14.000003', null, true, null, 'matthias@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, false, 'Matthias');
insert into users (id, created_at, updated_at, activated, avatarid, email, password_hash, reset_token, settings, status, tutorial_completed, username) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', '2022-05-13 13:13:14.000003', null, true, null, 'raffael@localhost', '$argon2id$v=19$m=4096,t=3,p=1$TTA7TLbieWng6w+igRYxyQ$rZT5h+Pu2qARIEjoJUB9ErNiv9zTnBcVcUn4l9Du1kg', null, null, 4, false, 'Raffael');

--Friends of Mete
insert into friends (user_id, friend_id) values ('6eec88ff-e643-4617-9d77-10cba6044050', '44cc3f8c-436b-47d8-896b-7563d60355b4');
insert into friends (user_id, friend_id) values ('6eec88ff-e643-4617-9d77-10cba6044050', 'c252e500-5d64-4ca6-86cf-e9c7483a2356');
insert into friends (user_id, friend_id) values ('6eec88ff-e643-4617-9d77-10cba6044050', 'f1961be8-fb94-4431-8262-eef1f71257fe');
insert into friends (user_id, friend_id) values ('6eec88ff-e643-4617-9d77-10cba6044050', 'e51fdd83-640e-4ba6-a953-bb8812e8abf8');
insert into friends (user_id, friend_id) values ('6eec88ff-e643-4617-9d77-10cba6044050', 'eebe92fa-d468-4f2d-85b5-014f7de962b5');

--Friends of Elvio
insert into friends (user_id, friend_id) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', '6eec88ff-e643-4617-9d77-10cba6044050');
insert into friends (user_id, friend_id) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', 'c252e500-5d64-4ca6-86cf-e9c7483a2356');
insert into friends (user_id, friend_id) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', 'f1961be8-fb94-4431-8262-eef1f71257fe');
insert into friends (user_id, friend_id) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', 'e51fdd83-640e-4ba6-a953-bb8812e8abf8');
insert into friends (user_id, friend_id) values ('44cc3f8c-436b-47d8-896b-7563d60355b4', 'eebe92fa-d468-4f2d-85b5-014f7de962b5');

--Friends of Jerome
insert into friends (user_id, friend_id) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', '6eec88ff-e643-4617-9d77-10cba6044050');
insert into friends (user_id, friend_id) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', '44cc3f8c-436b-47d8-896b-7563d60355b4');
insert into friends (user_id, friend_id) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', 'f1961be8-fb94-4431-8262-eef1f71257fe');
insert into friends (user_id, friend_id) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', 'e51fdd83-640e-4ba6-a953-bb8812e8abf8');
insert into friends (user_id, friend_id) values ('c252e500-5d64-4ca6-86cf-e9c7483a2356', 'eebe92fa-d468-4f2d-85b5-014f7de962b5');

--Friends of Matej
insert into friends (user_id, friend_id) values ('f1961be8-fb94-4431-8262-eef1f71257fe', '6eec88ff-e643-4617-9d77-10cba6044050');
insert into friends (user_id, friend_id) values ('f1961be8-fb94-4431-8262-eef1f71257fe', '44cc3f8c-436b-47d8-896b-7563d60355b4');
insert into friends (user_id, friend_id) values ('f1961be8-fb94-4431-8262-eef1f71257fe', 'c252e500-5d64-4ca6-86cf-e9c7483a2356');
insert into friends (user_id, friend_id) values ('f1961be8-fb94-4431-8262-eef1f71257fe', 'e51fdd83-640e-4ba6-a953-bb8812e8abf8');
insert into friends (user_id, friend_id) values ('f1961be8-fb94-4431-8262-eef1f71257fe', 'eebe92fa-d468-4f2d-85b5-014f7de962b5');

--Friends of Matthias
insert into friends (user_id, friend_id) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', '6eec88ff-e643-4617-9d77-10cba6044050');
insert into friends (user_id, friend_id) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', '44cc3f8c-436b-47d8-896b-7563d60355b4');
insert into friends (user_id, friend_id) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', 'c252e500-5d64-4ca6-86cf-e9c7483a2356');
insert into friends (user_id, friend_id) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', 'f1961be8-fb94-4431-8262-eef1f71257fe');
insert into friends (user_id, friend_id) values ('e51fdd83-640e-4ba6-a953-bb8812e8abf8', 'eebe92fa-d468-4f2d-85b5-014f7de962b5');

--Friends of Raffael
insert into friends (user_id, friend_id) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', '6eec88ff-e643-4617-9d77-10cba6044050');
insert into friends (user_id, friend_id) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', '44cc3f8c-436b-47d8-896b-7563d60355b4');
insert into friends (user_id, friend_id) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', 'c252e500-5d64-4ca6-86cf-e9c7483a2356');
insert into friends (user_id, friend_id) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', 'f1961be8-fb94-4431-8262-eef1f71257fe');
insert into friends (user_id, friend_id) values ('eebe92fa-d468-4f2d-85b5-014f7de962b5', 'e51fdd83-640e-4ba6-a953-bb8812e8abf8');