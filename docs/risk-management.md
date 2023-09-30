# Risk management
In every service that touches the topic of payments there are a lot of risks to consider.
This doc captures risks grouped by topic with mitigations.

## Risk - payment for fake game
This is the highest risk in this flow meaning the organiser organised game which he does not own or there is no game at
all.
This will result in money loss on players' side and unfair gain on organiser's side, causing significant reputation
damage for the platform.

### Mitigations:
- money will be transferred to location owner after specific time past game elapses and the game must be free of 
disputes before that time
- organiser must be marked as trusted either by administrator or by revealing and confirming real bank account 
information (premium)

## Risk - player did not come
In case of this scenario the player is scammer meaning he signs for the game and do not come causing insufficient number
of players to play a match or unbalanced teams.

### Mitigations
- players cannot sign out after specific time before the game
- player requires manual approval or being explicitly marked as trusted by organiser

## Risk - legality of software
System that operates on money must meet a lot of regulations in order to be legal and must be bulletproof.

### Mitigations:
- split development for two phases: one that operates on platform-specific coins and the integration with real payment
platform in second part
- at first phase player's virtual wallet will be filled by organiser in process called transfer registration which means 
that organiser assigns specific amount of coins witnessed by himself
- such coin will only be possible to use in games with matching organiser
