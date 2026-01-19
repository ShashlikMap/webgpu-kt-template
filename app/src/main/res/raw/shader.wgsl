struct Global {
    view_proj: mat4x4<f32>,
};
@group(0) @binding(0)
var<uniform> global: Global;

struct VertexInput {
    @location(0) position: vec3<f32>,
    @location(1) normal: vec3<f32>,
    @location(2) color: vec4<f32>,
}

struct VertexOutput {
    @builtin(position) clip_position: vec4<f32>,
    @location(1) color: vec4<f32>,
    @location(2) world_normal: vec3<f32>,
    @location(3) world_position: vec3<f32>,
}

@vertex fn vertexMain(model: VertexInput) -> VertexOutput {
    var out: VertexOutput;
    out.world_position = model.position;
    out.world_normal = model.normal;
    out.clip_position = global.view_proj * vec4<f32>(model.position.xyz, 1.0);
    out.color = model.color;
    return out;
}

const light_pos = normalize(vec3(5.0, 5.0, 5.0));
const light_color = vec3(1.0, 1.0, 1.0);
const ambient_color = vec3(0.3, 0.3, 0.3);

@fragment fn fragmentMain(in: VertexOutput) -> @location(0) vec4<f32> {
    let light_dir = normalize(light_pos - in.world_position);

    let diffuse_strength = max(dot(normalize(in.world_normal), light_dir), 0.0);
    let diffuse_color = light_color * diffuse_strength;

    let result_color = (ambient_color + diffuse_color) * in.color.rgb;

    return vec4(result_color.rgb, in.color.a);
}