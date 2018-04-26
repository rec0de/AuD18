# Codegolf-ish ruby script to generate random test data
count = ARGV[0].chomp.to_i # Usage: ruby generator.rb [number of lines]
o = [('0'..'9'), ('A'..'Z')].map(&:to_a).flatten # Create array of possible ID characters
# Create a range with count values, then transform every value into a random line (ABC12;ABC1;OK)
puts (1..count).map{ (0..4).map { o[rand(o.length)] }.join+';'+(0..3).map { o[rand(o.length)] }.join+';'+"OK\n" }.join