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
name, 
description, 
release_date, 
duration, 
rating_id
FROM film AS f
JOIN "like" AS l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY COUNT(l.user_id) DESC
LIMIT 10;

-- получение общих друзей (в данном случае пользователей с id = 3 и id = 4)
SELECT *
FROM "user"
WHERE user_id IN (
        SELECT friend_id 
	FROM friend_status 
	WHERE user_id = 3
        INTERSECT 
        SELECT friend_id 
	FROM friend_status 
	WHERE user_id = 4
        );



		
