🏗️ Arkitektur (MVVM)
Bruk StateFlow/SharedFlow i ViewModel i stedet for LiveData

Separér UI-logikk og forretningslogikk med Use Cases og Repository-mønster

Bruk remember og derivedStateOf for å unngå unødvendig recomposition

Unngå å sende hele ViewModel til composables – send kun state + events

Implementér dependency injection med Hilt eller Koin

🎨 UI & Design (Material 3)
Sett opp dynamisk fargetema med Material You (material3-adaptive)

Tilpass fargepaletten manuelt i Color.kt for å matche merkevaren

Bruk Surface og Card for Material 3-typografi og elevation

Implementér adaptive layouts med NavigationRail for store skjermer

Bruk AnimatedVisibility og AnimatedContent for myke overganger

Legg til RoundedCornerShape(16.dp) på Card, Button og Overflater

Bruk Modifier.shadow() for subtile skygger

Implementér Dark Theme med egne fargeverdier

🧭 Navigering
Bruk type-safe navigasjon med definerte ruter (sealed class/interface)

Opprett en NavigationGraph som håndterer alle destinasjoner

Bruk NavHostController for å styre navigasjon

Implementér deep linking med URI-håndtering

Bruk argumenter i navigasjon med @Serializable data classes

⚡ Ytelse
Unngå å lage objekter inne i composables – bruk remember

Bruk @Stable og @Immutable annotasjoner for å optimalisere recomposition

Unngå store data-samlinger i UI-state uten å bruke derivedStateOf

Bruk LazyColumn/LazyRow med key-parameter for lister

Unngå kompleks logikk i composables – flytt til ViewModel

🧪 Testing
Skriv enhetstester for ViewModel med JUnit og Turbine (for StateFlow)

Bruk Compose UI-tester med ComposeTestRule

Test navigasjon med NavController i UI-tester

Mock repository og use cases med MockK eller Mockito

