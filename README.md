# 🎓 Campus Virtual de Materias y Recursos de Estudio

Proyecto Final — **Tecnicatura Universitaria en Programación a Distancia (UTN)**

[![Java](https://img.shields.io/badge/Backend-Java%20%2F%20Spring%20Boot-brightgreen)](#-stack-tecnológico)
[![React](https://img.shields.io/badge/Frontend-React-61DAFB)](#-stack-tecnológico)
[![PostgreSQL](https://img.shields.io/badge/DB-PostgreSQL-336791)](#-stack-tecnológico)
[![Status](https://img.shields.io/badge/Estado-En%20desarrollo-yellow)]()

---

## 📌 Descripción

Los estudiantes suelen necesitar acceder de forma centralizada a los contenidos y materiales de estudio de sus materias (documentos, guías y videos), pero muchas veces esta información se encuentra dispersa en distintos canales (correo, grupos de mensajería, carpetas compartidas), lo que dificulta su organización y consulta.

Este proyecto propone el desarrollo de un **campus virtual web** que permita a un estudiante ingresar con su cuenta y acceder al contenido y material de estudio de las materias en las que está inscripto, incluyendo archivos (PDFs) y videos. El sistema cuenta además con un rol de **administrador/docente** encargado de cargar y mantener actualizado dicho contenido.

## 🎯 Objetivos

**Objetivo general**

Desarrollar una plataforma web tipo campus virtual que centralice el acceso al material de estudio de las materias de una institución educativa.

**Objetivos específicos**

- Permitir el registro e inicio de sesión diferenciando roles de estudiante y administrador/docente.
- Permitir a los estudiantes visualizar el listado de materias y su material asociado.
- Permitir a los estudiantes visualizar y descargar archivos, y acceder a videos del material de estudio.
- Permitir al rol administrador/docente crear y mantener materias y cargar material nuevo.

## 🧩 Alcance del proyecto (MVP)

| Módulo | Estudiante | Admin / Docente |
|---|---|---|
| **Login** | Accede y ve sus materias | Accede al panel de gestión |
| **Materias** | Ve listado y detalle | Crea y edita materias |
| **Material de estudio** | Ve y descarga archivos / videos | Sube archivos y enlaces de video |

**Fuera de alcance (mejoras futuras)**

- Foros de discusión y mensajería entre usuarios.
- Exámenes y evaluaciones en línea.
- Sistema de calificaciones.
- Videoconferencias en vivo.

## 🛠️ Stack tecnológico

| Área | Tecnología |
|---|---|
| **Backend** | Java + Spring Boot (Spring Data JPA para persistencia, Spring Security para autenticación y roles) |
| **Frontend** | React |
| **Base de datos** | PostgreSQL |
| **Almacenamiento** | Archivos (PDFs) en servicio de almacenamiento externo; videos enlazados desde plataforma externa (ej. YouTube en modo no listado) |
| **Despliegue** | Backend y base de datos en Render/Railway; frontend en Vercel/Netlify |
| **Control de versiones** | Git y GitHub |

## 🗓️ Plan de trabajo

| Etapa | Actividad | Duración estimada |
|---|---|---|
| 1 | Propuesta, elección de tutor, repositorio y diseño de base de datos | Semanas 1-2 |
| 2 | Desarrollo del backend: modelos, autenticación y API REST | Semanas 3-5 |
| 3 | Desarrollo del frontend: pantallas de login, materias y material | Semanas 5-7 |
| 4 | Integración frontend-backend y carga de contenido de prueba | Semana 8 |
| 5 | Pruebas, corrección de errores y despliegue | Semana 9 |
| 6 | Documentación final y preparación de la presentación | Semana 10 |

> El plan es una estimación inicial y podrá ajustarse junto con el tutor una vez aprobada la propuesta.

## 👥 Equipo

- Daniel Alfredo Oscar Alderete
- Tadeo Oscar Acosta

**Tutor propuesto:** Oscar Londero

## 📂 Repositorio

Todo el desarrollo del proyecto (backend, frontend y documentación) se aloja en este repositorio.

---

<p align="center">Proyecto Final — Tecnicatura Universitaria en Programación a Distancia, UTN</p>
