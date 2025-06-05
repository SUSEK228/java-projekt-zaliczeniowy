# **Projekt zaliczeniowy – Finance Tracker**

> **Aplikacja desktopowa JavaFX**  
> **Autorzy:** Dominik Pakuła (322960) • Paweł Kulesza (322948)

---

## **Nazwa projektu**
**Finance Tracker** – narzędzie do prowadzenia domowej księgowości.

## **Skład zespołu**
- **Osoba 1** Dominik Pakuła (322960)  
- **Osoba 2** Paweł Kulesza (322948)

---

## **Wymagania dotyczące projektu**
Projekt spełnia następujące kryteria:

- **Działający kod** przechowywany w repozytorium, zawierający testy jednostkowe (JUnit 5).
- **Dokumentacja** obejmująca:  
  • autorów,  
  • tematykę projektu,  
  • wykorzystane technologie,  
  • opis działania programu,  
  • spis wykorzystanych materiałów.  
- **Termin oddania**: przedostatnie zajęcia laboratoryjne.

---

## **Spis treści**
1. [Opis projektu](#opis-projektu)  
2. [Kluczowe funkcjonalności](#kluczowe-funkcjonalności)  
3. [Stack technologiczny](#stack-technologiczny)  
4. [Wymagania systemowe](#wymagania-systemowe)  
5. [Instrukcja instalacji](#instrukcja-instalacji)  
6. [Instrukcja uruchomienia](#instrukcja-uruchomienia)  
7. [Testy jednostkowe](#testy-jednostkowe)  
8. [Struktura projektu](#struktura-projektu)  
9. [Plany rozwoju](#plany-rozwoju)  
10. [Licencja](#licencja)

---

## Opis projektu
**Finance Tracker** umożliwia rejestrowanie przychodów i wydatków, prezentuje podsumowania (saldo, przychody, koszty) oraz statystyki wydatków według kategorii. Aplikacja wspiera limity dzienne/tygodniowe/miesięczne i ostrzega o ich przekroczeniu.

## Kluczowe funkcjonalności
- Rejestracja transakcji (opis, kwota, kategoria, data)
- Tabela transakcji z filtrowaniem i możliwością usuwania wpisów
- Podsumowania finansowe (saldo, przychody, wydatki miesiąca)
- Limity wydatków z alertem przekroczenia
- Statystyki: bilans i wykres kołowy wydatków według kategorii
- Ciemny, responsywny interfejs (JavaFX + CSS)

## Stack technologiczny
| Warstwa        | Technologia |
|----------------|-------------|
| Język          | **Java 17** |
| UI             | **JavaFX 17** (FXML, CSS) |
| Baza danych    | **SQLite** + JDBC |
| Testy          | **JUnit 5** |
| Build/Dependency Mgmt | *Maven* lub *Gradle* |

## Wymagania systemowe
- JDK 17 lub nowszy
- JavaFX SDK 17 (jeśli używasz VM options)
- Sterownik `sqlite-jdbc` w classpath
- System: Windows / macOS / Linux

## Instrukcja instalacji
1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/<user>/FinanceTracker.git
Importuj projekt w IDE (IntelliJ IDEA / Eclipse / NetBeans).

Dodaj biblioteki JavaFX i sqlite-jdbc (Maven/Gradle – zależności w pom.xml lub build.gradle).

Instrukcja uruchomienia
Upewnij się, że finance_tracker.db może zostać utworzony w katalogu roboczym.

bash
Kopiuj
Edytuj
# Przykładowe VM options
--module-path <ścieżka_do_javafx>/lib --add-modules javafx.controls,javafx.fxml

# IntelliJ IDEA
Run ▶ Main.java
Testy jednostkowe
bash
Kopiuj
Edytuj
mvn test    # lub  gradle test
Struktura projektu
css
Kopiuj
Edytuj
FinanceTracker/
├── src/main/java/com/financetracker/financetracker/
│   ├── Main.java
│   ├── MainController.java
│   ├── Transaction.java
│   └── Database.java
├── src/main/resources/
│   ├── main.fxml
│   └── style.css
└── pom.xml  # lub build.gradle
Plany rozwoju
Obsługa wielu walut i kursów wymiany

Eksport/import danych (CSV, XLSX)

Raporty PDF oraz powiadomienia e-mail o limitach

Synchronizacja danych w chmurze
