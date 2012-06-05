# Get the directory that this configuration file exists in
dir = File.dirname(__FILE__)

# Load the sencha-touch framework automatically.
load File.join(dir, 'themes')

# Compass configurations
css_path     = File.join(dir, "..", "assets", "www", "resources", "css")
sass_path    = File.join(dir, "scss")
environment  = :production
output_style = :compressed
