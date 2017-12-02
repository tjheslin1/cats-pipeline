package io.github.tjheslin1.dogs

case class Dog(name: String, colour: String, size: DogSize)

sealed trait DogSize

object Small extends DogSize
object Medium extends DogSize
object Large extends DogSize