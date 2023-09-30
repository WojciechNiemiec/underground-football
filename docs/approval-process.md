# Player approval process

The process of player approval depends on trustworthiness of player and settlement strategy.
Player that was marked as trusted to organiser will be automatically approved.
Others will require manual approval from the organiser.

```mermaid
flowchart TD
    START((Start)) --> SP[Sign player]
    SP --> IPT{Is player trusted?}
    IPT -->|yes| CP[Confirm player]
    IPT -->|no| AC[Await confirmation]
    AC --> OD{Organiser's decision}
    OD -->|confirm| CP
    OD -->|ignore| AC
    OD -->|reject| RP[Reject player]
    CP --> CSS{Check settlement\n strategy}
    CSS -->|Payment before game| AP[Await payment]
    AP --> PPOT{Player pays\n on time?}
    PPOT -->|yes| STOP((Stop))
    PPOT -->|no| RP
    RP --> STOP
    CSS -->|Payment after game| STOP
```

## Comparison of features for settlement strategies.

| Feature      | Automatic              | Manual                           |
|--------------|------------------------|----------------------------------|
| Is premium   | yes                    | no                               |
| Debt allowed | no                     | yes                              |
| Real money   | yes (bank transaction) | no (coins assigned by organiser) |
