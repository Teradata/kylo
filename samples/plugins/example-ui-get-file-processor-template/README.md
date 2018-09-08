Custom Processor Templates
==========================

Kylo allows you to use your own custom Angular component when customizing a NiFi processor in the Kylo feed wizard.

1. Create a JSON file with the suffix "processor-template-definition.json".  Usually its good to prefix this file with the name of the processor (i.e. "get-file-processor-template-definition.json")

```json
{
  "processorDisplayName": "An optional String to have the code Match on the processorType and the label of the processor.  If Null or not set it will just use the processorType to match when rendering the template",
  "processorTypes": ["Array of the fully qualified NiFi Processor class name(s)"],
  "module": "Path and name of the module containing the custom Angular component"
}
```

An example JSON file is here:

```json
{
  "processorTypes": ["org.apache.nifi.processors.standard.GetFile"],
  "module": "js/plugin/processor-templates/get-file/bundle/get-file.umd.min"
}
```

2. The Angular module

The module should include a `ProcessorControl` that references the component to use for the processor:

```typescript
import {NgModule} from "@angular/core";
import {ProcessorControl} from "@kylo/feed";

@NgModule({
    declarations: [
        GetFileComponent
    ],
    entryComponents: [
        GetFileComponent
    ],
    imports: [
        KyloFeedModule
    ],
    providers: [
        {provide: ProcessorControl, useValue: new ProcessorControl(GetFileComponent), multi: true}
    ]
})
export class GetFileModule {
}

```

3. The Angular component

A `ProcessorRef` will be injected into the component. This contains the processor model and can be used to add controls to the feed's form.

```typescript
import {Component} from "@angular/core";
import {ProcessorRef} from "@kylo/feed";

@Component({
    template: `<div>Processor: {{processor.name}}</div>`
})
export class GetFileComponent {
    
    constructor(readonly processor: ProcessorRef) {
    }
}
```
