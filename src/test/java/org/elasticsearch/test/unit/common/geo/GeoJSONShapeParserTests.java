begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.test.unit.common.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|common
operator|.
name|geo
package|;
end_package

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Shape
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|jts
operator|.
name|JtsGeometry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|jts
operator|.
name|JtsPoint
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoJSONShapeParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoShapeConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|testng
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_comment
comment|/**  * Tests for {@link GeoJSONShapeParser}  */
end_comment

begin_class
DECL|class|GeoJSONShapeParserTests
specifier|public
class|class
name|GeoJSONShapeParserTests
block|{
DECL|field|GEOMETRY_FACTORY
specifier|private
specifier|final
specifier|static
name|GeometryFactory
name|GEOMETRY_FACTORY
init|=
operator|new
name|GeometryFactory
argument_list|()
decl_stmt|;
annotation|@
name|Test
DECL|method|testParse_simplePoint
specifier|public
name|void
name|testParse_simplePoint
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|pointGeoJson
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"Point"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"coordinates"
argument_list|)
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Point
name|expected
init|=
name|GEOMETRY_FACTORY
operator|.
name|createPoint
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.0
argument_list|,
literal|0.0
argument_list|)
argument_list|)
decl_stmt|;
name|assertGeometryEquals
argument_list|(
operator|new
name|JtsPoint
argument_list|(
name|expected
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
argument_list|,
name|pointGeoJson
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParse_lineString
specifier|public
name|void
name|testParse_lineString
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|lineGeoJson
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"LineString"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"coordinates"
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Coordinate
argument_list|>
name|lineCoordinates
init|=
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
decl_stmt|;
name|lineCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|lineCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|LineString
name|expected
init|=
name|GEOMETRY_FACTORY
operator|.
name|createLineString
argument_list|(
name|lineCoordinates
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|lineCoordinates
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|assertGeometryEquals
argument_list|(
operator|new
name|JtsGeometry
argument_list|(
name|expected
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|false
argument_list|)
argument_list|,
name|lineGeoJson
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParse_polygonNoHoles
specifier|public
name|void
name|testParse_polygonNoHoles
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|polygonGeoJson
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"Polygon"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"coordinates"
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Coordinate
argument_list|>
name|shellCoordinates
init|=
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
decl_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|LinearRing
name|shell
init|=
name|GEOMETRY_FACTORY
operator|.
name|createLinearRing
argument_list|(
name|shellCoordinates
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|shellCoordinates
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Polygon
name|expected
init|=
name|GEOMETRY_FACTORY
operator|.
name|createPolygon
argument_list|(
name|shell
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertGeometryEquals
argument_list|(
operator|new
name|JtsGeometry
argument_list|(
name|expected
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|false
argument_list|)
argument_list|,
name|polygonGeoJson
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParse_polygonWithHole
specifier|public
name|void
name|testParse_polygonWithHole
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|polygonGeoJson
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"Polygon"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"coordinates"
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.2
argument_list|)
operator|.
name|value
argument_list|(
literal|0.2
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.8
argument_list|)
operator|.
name|value
argument_list|(
literal|0.2
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.8
argument_list|)
operator|.
name|value
argument_list|(
literal|0.8
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.2
argument_list|)
operator|.
name|value
argument_list|(
literal|0.8
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.2
argument_list|)
operator|.
name|value
argument_list|(
literal|0.2
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Coordinate
argument_list|>
name|shellCoordinates
init|=
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
decl_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|shellCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Coordinate
argument_list|>
name|holeCoordinates
init|=
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
decl_stmt|;
name|holeCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.2
argument_list|,
literal|0.2
argument_list|)
argument_list|)
expr_stmt|;
name|holeCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.8
argument_list|,
literal|0.2
argument_list|)
argument_list|)
expr_stmt|;
name|holeCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.8
argument_list|,
literal|0.8
argument_list|)
argument_list|)
expr_stmt|;
name|holeCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.2
argument_list|,
literal|0.8
argument_list|)
argument_list|)
expr_stmt|;
name|holeCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100.2
argument_list|,
literal|0.2
argument_list|)
argument_list|)
expr_stmt|;
name|LinearRing
name|shell
init|=
name|GEOMETRY_FACTORY
operator|.
name|createLinearRing
argument_list|(
name|shellCoordinates
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|shellCoordinates
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|LinearRing
index|[]
name|holes
init|=
operator|new
name|LinearRing
index|[
literal|1
index|]
decl_stmt|;
name|holes
index|[
literal|0
index|]
operator|=
name|GEOMETRY_FACTORY
operator|.
name|createLinearRing
argument_list|(
name|holeCoordinates
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|holeCoordinates
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Polygon
name|expected
init|=
name|GEOMETRY_FACTORY
operator|.
name|createPolygon
argument_list|(
name|shell
argument_list|,
name|holes
argument_list|)
decl_stmt|;
name|assertGeometryEquals
argument_list|(
operator|new
name|JtsGeometry
argument_list|(
name|expected
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|false
argument_list|)
argument_list|,
name|polygonGeoJson
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParse_multiPoint
specifier|public
name|void
name|testParse_multiPoint
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|multiPointGeoJson
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"MultiPoint"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"coordinates"
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|100.0
argument_list|)
operator|.
name|value
argument_list|(
literal|0.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|101.0
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Coordinate
argument_list|>
name|multiPointCoordinates
init|=
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
decl_stmt|;
name|multiPointCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|100
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|multiPointCoordinates
operator|.
name|add
argument_list|(
operator|new
name|Coordinate
argument_list|(
literal|101
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|MultiPoint
name|expected
init|=
name|GEOMETRY_FACTORY
operator|.
name|createMultiPoint
argument_list|(
name|multiPointCoordinates
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|multiPointCoordinates
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|assertGeometryEquals
argument_list|(
operator|new
name|JtsGeometry
argument_list|(
name|expected
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|false
argument_list|)
argument_list|,
name|multiPointGeoJson
argument_list|)
expr_stmt|;
block|}
DECL|method|assertGeometryEquals
specifier|private
name|void
name|assertGeometryEquals
parameter_list|(
name|Shape
name|expected
parameter_list|,
name|String
name|geoJson
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|geoJson
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|GeoJSONShapeParser
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

