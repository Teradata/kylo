import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";

export class SparkDatasetScriptBuilderService {

    /*



    private static String asOptions(List<String> options, String var){
        return options != null ? options.stream().collect(Collectors.joining("\")." + var + "(\"", "." + var + "(\"", "\")")) : "";
    }
    private static String asOptions(Map<String,String> options, String var) {
        return options != null && !options.isEmpty() ? options.entrySet().stream().map(entrySet -> "\"" + entrySet.getKey() + "\",\"" + StringEscapeUtils.escapeJava(entrySet.getValue()) + "\"").collect(Collectors.joining(")." + var + "(", "." + var + "(", ")")) : "";
    }


    public static String asScalaScript(KyloCatalogReadRequest request){

        int previewLimit = request.getPageSpec() != null ? request.getPageSpec().getNumRows() : 20;
        String format = request.getFormat();

        StringBuilder sb = new StringBuilder();
        sb.append("import org.apache.spark.sql._\n");
        sb.append(" import com.thinkbiganalytics.kylo.catalog._\n");
        sb.append(" var builder = KyloCatalog.builder(sqlContext)\n");
        sb.append("var client = builder.build()\n");

        String addFiles = asOptions(request.getFiles(),"addFile");
        String addJars = asOptions(request.getJars(),"addJar");
        String addOptions = asOptions(request.getOptions(),"option");
        String path = request.getPaths() != null && !request.getPaths().isEmpty() ? request.getPaths().get(0) : "";

        sb.append(String.format(" var reader = client.read%s.format(\"%s\")%s%s\n",addOptions,format,addFiles,addJars));
        sb.append("var df = reader.load().limit("+previewLimit+")\n");
        sb.append("df");


        String script = sb.toString();
        return script;

    }


     */

    build(datasets:PreviewDataSet[], dataFrameVaraible:string){

    }
}