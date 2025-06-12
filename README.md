# java-filmorate
Template repository for Filmorate project.

![Database schema](/schema.png)

Примеры запросов

-- получение всех фильмов
SELECT *
FROM film;

-- получение всех пользователей
SELECT *
FROM "user";

-- получение N наиболее популярных фильмов (в данном случае 10)
SELECT f.film_id,
f."name",
f.description,
f.release_date,
f.duration,
f.rating_id
FROM film f
JOIN "like" l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY COUNT(f.film_id) DESC
LIMIT 10;

-- получение общих друзей (в данном случае пользователей с id = 3 и id = 4)
SELECT *
FROM "user" u
WHERE u.user_id IN (
	SELECT fs1.friend_id
	FROM "user" u
	JOIN friend_status fs1 ON u.user_id = fs1.user_id
	WHERE fs1.status = 'confirmed' AND fs1.user_id = 3
	INTERSECT
	SELECT fs1.friend_id
	FROM "user" u
	JOIN friend_status fs1 ON u.user_id = fs1.user_id
	WHERE fs1.status = 'confirmed' AND fs1.user_id = 4);
