package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre.Genre

case class Album(
  id: String,
  title: String,
  genre: Genre
)
