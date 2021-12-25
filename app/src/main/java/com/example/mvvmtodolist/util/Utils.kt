package com.example.mvvmtodolist.util

// trick from https://proandroiddev.com/til-when-is-when-exhaustive-31d69f630a8b
// expression vs statement: https://blog.kotlin-academy.com/kotlin-programmer-dictionary-statement-vs-expression-e6743ba1aaa0
val <T> T.exhaustive: T
    get() = this
